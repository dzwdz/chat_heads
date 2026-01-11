package dzwdz.chat_heads;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import dzwdz.chat_heads.config.ChatHeadsConfig;
import dzwdz.chat_heads.config.ChatHeadsConfigDefaults;
import dzwdz.chat_heads.config.ClothConfigCommonImpl;
import dzwdz.chat_heads.mixininterface.HeadRenderable;
import dzwdz.chat_heads.mixininterface.Ownable;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent.ChatGraphicsAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

import static dzwdz.chat_heads.config.RenderPosition.BEFORE_LINE;
import static dzwdz.chat_heads.config.SenderDetection.HEURISTIC_ONLY;
import static dzwdz.chat_heads.config.SenderDetection.UUID_ONLY;

/*
 * small changes in 1.20.5:
 *
 * addMessage(Component, MessageSignature, int, GuiMessageTag, boolean refreshing)
 * was split into
 * addMessageToDisplayQueue(GuiMessage)
 * addMessageToQueue(GuiMessage)
 * the latter was previously run when refreshing = false
 *
 *
 * Call stack looks roughly like this:
 *
 * ClientPacketListener.handlePlayerChat()
 *  -> ChatListener.handlePlayerChatMessage(), note: doesn't take PlayerInfo but GameProfile instead
 *  -> ChatListener.showMessageToPlayer()
 *  -> ChatComponent.addMessage(), new GuiMessage()
 *  -> ChatComponent.addMessageToDisplayQueue()
 *  -> new GuiMessage.Line()
 *
 * ClientPacketListener.handleDisguisedChat()
 *  -> ChatListener.handleDisguisedChatMessage()
 *  -> ChatComponent.addMessage(), new GuiMessage()
 *  -> ChatComponent.addMessageToDisplayQueue()
 *  -> new GuiMessage.Line()
 *
 * ClientPacketListener.handleSystemChat()
 *  -> ChatListener.handleSystemMessage()
 *  -> ChatComponent.addMessage(), new GuiMessage()
 *  -> ChatComponent.addMessageToDisplayQueue()
 *  -> new GuiMessage.Line()
 *
 * FreedomChat (https://github.com/Oharass/FreedomChat) will likely work the same as before, converting chat messages
 * to system messages, so we still handle those.
 */

public class ChatHeads {
    public static final String MOD_ID = "chat_heads";
    public static final Pattern FORMAT_REGEX = Pattern.compile("§.");
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final Identifier DISABLE_RESOURCE = Identifier.fromNamespaceAndPath(MOD_ID, "disable");

    public static ChatHeadsConfig CONFIG = new ChatHeadsConfigDefaults();

    @NotNull public static HeadData lastSenderData = HeadData.EMPTY;

    // with Compact Chat, addMessageToDisplayQueue() calls refreshTrimmedMessage() and thus addMessageToDisplayQueue() with another owner inside itself,
    // we hence need two separate owner variables, distinguished by 'refreshing'
    public static boolean refreshing;
    @NotNull public static HeadData lineData = HeadData.EMPTY;
    @NotNull public static HeadData refreshingLineData = HeadData.EMPTY;

    public static volatile boolean serverSentUuid = false;
    public static volatile boolean serverDisabledChatHeads = false;

    // for "before line" / direct rendering
    public static final Set<Identifier> blendedHeadTextures = new HashSet<>();
    public static GuiGraphics guiGraphics = null;
    public static ChatGraphicsAccess chatGraphicsAccess = null;

    // for "before name" rendering aka vanilla PlayerSprites rendering:
    // custom rendering means two things: adjusting the padding (see PaddedChatGlyph) and optional 3Dness (see PlayerGlyphProviderInstanceMixin)
    // these two things happen at different times, making it hard to precisely limit to just the chat and the preview in the config menu
    public static boolean customHeadRendering;

    public static void init() {
        if (Compat.isClothConfigLoaded()) {
            ClothConfigCommonImpl.loadConfig();
        }
    }

    @NotNull
    public static HeadData getLineData() {
        return refreshing ? refreshingLineData : lineData;
    }

    public static void setLineData(@NotNull HeadData headData) {
        if (refreshing) {
            refreshingLineData = headData;
        } else {
            lineData = headData;
        }
    }

    // On EssentialsX, /realname returns "<nickname> is <profile name>", which we can use to auto-add name aliases
    public static void autoDetectAlias(Component message) {
        String text = message.getString();

        int i = text.indexOf(" ");
        if (i == -1) return;

        var nickname = text.substring(0, i);

        if (!text.substring(i).startsWith(" is "))
            return;

        var profileName = text.substring(i + " is ".length());

        if (profileName.contains(" "))
            return;

        ChatHeads.CONFIG.addNameAlias(nickname, profileName);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static Component handleAddedMessage(Component originalMessage, @Nullable PlayerInfo playerInfo) {
        if (ChatHeads.CONFIG.detectNameAliases())
            autoDetectAlias(originalMessage);

        // note: lastSenderData is used for the BEFORE_LINE rendering, the returned message for the BEFORE_NAME rendering
        ChatHeads.lastSenderData = HeadData.EMPTY;

        if (ChatHeads.serverDisabledChatHeads)
            return originalMessage;

        boolean forceHeuristic = isShowcaseItemMessage(originalMessage);

        if (ChatHeads.CONFIG.senderDetection() == HEURISTIC_ONLY || forceHeuristic) {
            // don't use any prior knowledge
            playerInfo = null;
        } else {
            if (playerInfo != null) {
                ChatHeads.serverSentUuid = true;
            } else {
                // no PlayerInfo/UUID, message is either not from a player or the server didn't wanna tell

                if (ChatHeads.CONFIG.senderDetection() == UUID_ONLY || ChatHeads.serverSentUuid && ChatHeads.CONFIG.smartHeuristics()) {
                    return originalMessage;
                }
            }
        }

        var messageAndData = detectPlayerAndAddChatHead(originalMessage, playerInfo);
        if (messageAndData == null)
            return originalMessage;

        var decoratedMessage = messageAndData.getFirst();
        var headData = messageAndData.getSecond();

        ChatHeads.lastSenderData = headData;

        if (ChatHeads.CONFIG.renderPosition() == BEFORE_LINE) {
            return originalMessage;
        } else {
            return decoratedMessage;
        }
    }

    /**
     * Returns <code>message<code/> decorated with a chat head and the <code>PlayerInfo<code/> used for the head.
     * Returns <code>null<code/> if no head was/should be added.
     */
    @Nullable
    public static Pair<Component, HeadData> detectPlayerAndAddChatHead(Component message, @Nullable PlayerInfo givenPlayerInfo) {
        var connection = Minecraft.getInstance().getConnection();

        // When Polymer's early play networking API is used, messages can be received pre-login, in which case we disable chat heads
        if (connection == null)
            return null;

        var playerInfoCache = new PlayerInfoCache(connection);

        if (givenPlayerInfo != null) {
            playerInfoCache.addProfileName(givenPlayerInfo);
        } else {
            playerInfoCache.collectProfileNames();
        }

        // to simplify processing massively, the component tree graph is turned into a list of lone components, without siblings
        var split = ComponentProcessor.split(message);

        // (A4) don't place a head if there already is one
        if (ComponentProcessor.containsPlayerSprite(split))
            return null;

        PlayerInfo foundPlayerInfo = ComponentProcessor.addChatHeadForClickTellCommand(split, playerInfoCache);
        if (foundPlayerInfo != null)
            return new Pair<>(ComponentProcessor.join(split), HeadData.of(foundPlayerInfo));

        if (givenPlayerInfo != null) {
            playerInfoCache.add(givenPlayerInfo);
        } else {
            playerInfoCache.collectAllNames();
        }

        foundPlayerInfo = ComponentProcessor.addChatHeadForPlayerName(split, playerInfoCache);
        if (foundPlayerInfo != null)
            return new Pair<>(ComponentProcessor.join(split), HeadData.of(foundPlayerInfo));

        // fallback: put head at the front
        if (givenPlayerInfo != null) {
            var chatHead = ComponentProcessor.createChatHeadComponent(givenPlayerInfo, message);
            var decorated = Component.empty().append(chatHead).append(message);

            return new Pair<>(decorated, HeadData.of(givenPlayerInfo));
        }

        return null;
    }

    private static boolean isShowcaseItemMessage(Component message) {
        return message.getContents() instanceof TranslatableContents contents
                && Objects.equals(contents.getKey(), "showcaseitem.misc.shared_item");
    }

    @NotNull
    public static HeadData getHeadData(@NotNull GuiMessage.Line guiMessage) {
        return ((HeadRenderable) (Object) guiMessage).chatheads$getHeadData();
    }

    @NotNull
    public static HeadData getHeadData(@NotNull GuiMessage guiMessage) {
        return ((HeadRenderable) (Object) guiMessage).chatheads$getHeadData();
    }

    @Nullable
    public static PlayerInfo getOwner(@NotNull PlayerChatMessage message) {
        return ((Ownable) (Object) message).chatheads$getOwner();
    }

    public static void setHeadData(@NotNull GuiMessage guiMessage, @NotNull HeadData data) {
        ((HeadRenderable) (Object) guiMessage).chatheads$setHeadData(data);
    }

    public static void setOwner(@NotNull PlayerChatMessage message, PlayerInfo owner) {
        ((Ownable) (Object) message).chatheads$setOwner(owner);
    }

    public static boolean offsetChat(@NotNull HeadData headData) {
        if (ChatHeads.CONFIG.renderPosition() != BEFORE_LINE)
            return false;

        return headData != HeadData.EMPTY || (ChatHeads.CONFIG.offsetNonPlayerText() && !ChatHeads.serverDisabledChatHeads);
    }

    public static int getChatOffset(@NotNull HeadData headData) {
        return offsetChat(headData) ? headWidth() : 0;
    }

    public static int getTextWidthDifference(@NotNull GuiMessage.Line guiMessage) {
        return getTextWidthDifference(getHeadData(guiMessage));
    }

    public static int getTextWidthDifference(@NotNull HeadData headData) {
        if (ChatHeads.CONFIG.renderPosition() != BEFORE_LINE)
            return 0;

        // whenever a head is rendered or chat is being offset
        return headData != HeadData.EMPTY || offsetChat(headData) ? headWidth() : 0;
    }

    public static int headWidth() {
        return headWidth(ChatHeads.CONFIG.drawShadow());
    }

    // pixels the head takes up (including padding)
    public static int headWidth(boolean drawShadow) {
        return 8 + 2 + (drawShadow ? 1 : 0);
    }

    @NotNull
    public static HeadData scanForPlayerName(@NotNull String message, PlayerInfoCache playerInfoCache) {
        // large optimization: prepare a names lookup to improve worst case runtime of the following triple nested loop
        var namesByFirstCharacter = playerInfoCache.createNamesByFirstCharacterMap();

        boolean insideWord = false;

        // scan through the message code point by code point
        int[] messageSeq = message.codePoints().toArray();
        for (int i = 0; i < messageSeq.length; i++) {
            int c = messageSeq[i];

            // don't match words inside words ("tom" shouldn't match "custom")
            if (insideWord && isWordCharacter(c))
                continue; // note: don't need to update insideWord

            // try to match with names starting with the given character
            for (var name : namesByFirstCharacter.getOrDefault(c, List.of())) {
                int[] nameSeq = name.codePoints().toArray();

                // nothing left to match
                if (i + nameSeq.length-1 >= messageSeq.length)
                    continue;

                // don't match word ending with more word characters ("tom" shouldn't match "tomato")
                boolean nameEndsAsWord = isWordCharacter(nameSeq[nameSeq.length - 1]);
                boolean nameIsFollowedByWord = i + nameSeq.length < messageSeq.length && isWordCharacter(messageSeq[i + nameSeq.length]);
                if (nameEndsAsWord && nameIsFollowedByWord)
                    continue;

                if (containsSubsequenceAt(messageSeq, i, nameSeq)) {
                    return new HeadData(playerInfoCache.get(name), i);
                }
            }

            insideWord = isWordCharacter(c);
        }

        return HeadData.EMPTY;
    }

    public static class PlayerInfoCache {
        private final ClientPacketListener connection;
        private final Map<String, PlayerInfo> playerInfos = new HashMap<>();
        private boolean collectedProfileNames = false;
        private boolean collectedEverything = false;

        public PlayerInfoCache(@NotNull ClientPacketListener connection) {
            this.connection = connection;
        }

        public void collectProfileNames() {
            if (collectedProfileNames) return;
            collectedProfileNames = true;

            for (var playerInfo : connection.getOnlinePlayers()) {
                addProfileName(playerInfo);
            }
        }

        private void addProfileName(PlayerInfo playerInfo) {
            // plugins like HaoNick can change profile names to contain illegal characters like formatting codes
            String profileName = FORMAT_REGEX.matcher(playerInfo.getProfile().name()).replaceAll("");
            if (profileName.isEmpty())
                return;

            playerInfos.put(profileName, playerInfo);
        }

        public void collectAllNames() {
            if (collectedEverything) return;
            collectedEverything = true;

            collectProfileNames();

            // collect display names
            for (var playerInfo : connection.getOnlinePlayers()) {
                addDisplayName(playerInfo);
            }

            // add name aliases, copying player info from profile/display names
            addNameAliases();
        }

        private void addNameAliases() {
            for (var entry : CONFIG.getNameAliases().entrySet()) {
                PlayerInfo playerInfo = playerInfos.get(entry.getValue());
                if (playerInfo != null) {
                    playerInfos.putIfAbsent(entry.getKey(), playerInfo);
                }
            }
        }

        private void addDisplayName(PlayerInfo playerInfo) {
            if (playerInfo.getTabListDisplayName() != null) {
                String displayName = FORMAT_REGEX.matcher(playerInfo.getTabListDisplayName().getString()).replaceAll("");
                if (displayName.isEmpty())
                    return;

                playerInfos.putIfAbsent(displayName, playerInfo);
            }
        }

        public void add(PlayerInfo playerInfo) {
            addProfileName(playerInfo);
            addDisplayName(playerInfo);
            addNameAliases();
        }

        public Map<Integer, List<String>> createNamesByFirstCharacterMap() {
            Map<Integer, List<String>> namesByFirstCharacter = new HashMap<>();

            for (var name : playerInfos.keySet()) {
                namesByFirstCharacter.compute(name.codePointAt(0), (key, value) -> {
                    if (value == null) value = new ArrayList<>();
                    value.add(name);
                    return value;
                });
            }

            return namesByFirstCharacter;
        }

        @Nullable
        public PlayerInfo get(@NotNull String name) {
            return playerInfos.get(name);
        }

        public Set<String> getNames() {
            return playerInfos.keySet();
        }
    }

    private static boolean isWordCharacter(int codePoint) {
        return Character.isLetterOrDigit(codePoint) || codePoint == '_' || Character.getNumericValue(codePoint) != -1;
    }

    private static boolean containsSubsequenceAt(int[] sequence, int startIndex, int[] subsequence) {
        // assumes startIndex + sequence.length-1 < subsequence.length

        for (int j = 0; j < subsequence.length; j++) {
            if (sequence[startIndex + j] != subsequence[j]) {
                return false;
            }
        }

        return true;
    }

    public static NativeImage extractBlendedHead(NativeImage skin) {
        // workaround for CustomSkinLoader. at this point legacy skins are normally already converted to squares, but this appears to have broken in 1.21.4
        boolean isLegacy = skin.getWidth() / 2 == skin.getHeight();

        // vanilla skins are 64x64 pixels, HD skins (e.g. with CustomSkinLoader) 128x128
        int xScale = skin.getWidth() / 64;
        int yScale = skin.getHeight() / (isLegacy ? 32 : 64);

        NativeImage head = new NativeImage(8 * xScale, 8 * yScale, false);

        for (int y = 0; y < head.getHeight(); y++) {
            for (int x = 0; x < head.getWidth(); x++) {
                int headColor = skin.getPixel(8 * xScale + x, 8 * yScale + y);
                int hatColor = skin.getPixel(40 * xScale + x, 8 * yScale + y);

                // blend layers together
                head.setPixel(x, y, blendColors(headColor, hatColor));
            }
        }

        return head;
    }

    /** blend color2 onto color1 */
    public static int blendColors(int color1, int color2) {
        // note: this also works for ABGR
        float a1 = ARGB.alpha(color1) / 255f;
        float r1 = ARGB.red(color1)   / 255f;
        float g1 = ARGB.green(color1) / 255f;
        float b1 = ARGB.blue(color1)  / 255f;

        float a2 = ARGB.alpha(color2) / 255f;
        float r2 = ARGB.red(color2)   / 255f;
        float g2 = ARGB.green(color2) / 255f;
        float b2 = ARGB.blue(color2)  / 255f;

        // if a2 is 1, take color2, if it is 0, take color1
        float a3 = a2 * a2 + (1 - a2) * a1;
        float r3 = a2 * r2 + (1 - a2) * r1;
        float g3 = a2 * g2 + (1 - a2) * g1;
        float b3 = a2 * b2 + (1 - a2) * b1;

        // unsure if clamping is needed, better safe than sorry
        return ARGB.color(
            (int) Math.clamp(a3 * 255f, 0, 255f),
            (int) Math.clamp(r3 * 255f, 0, 255f),
            (int) Math.clamp(g3 * 255f, 0, 255f),
            (int) Math.clamp(b3 * 255f, 0, 255f)
        );
    }

    public static Identifier getBlendedHeadLocation(Identifier skinLocation) {
        return Identifier.fromNamespaceAndPath(ChatHeads.MOD_ID, skinLocation.getPath());
    }

    public static void renderChatHead(GuiGraphics guiGraphics, int x, int y, PlayerInfo owner, float opacity) {
        renderChatHead(guiGraphics, x, y, owner, opacity, ChatHeads.CONFIG.drawShadow());
    }

    public static void renderChatHead(GuiGraphics guiGraphics, int x, int y, PlayerInfo owner, float opacity, boolean drawShadow) {
        Identifier skinLocation = owner.getSkin().body().texturePath();

        int color = ARGB.white(opacity);
        int shadowColor = ARGB.scaleRGB(color, 0.25F);
        int shadowOffset = drawShadow ? -1 : 0;

        ClientLevel level = Minecraft.getInstance().level;
        Player player = level != null ? level.getPlayerByUUID(owner.getProfile().id()) : null;
        boolean upsideDown = player != null && AvatarRenderer.isPlayerUpsideDown(player);

        boolean showHat = owner.showHat();

        int yOffset = (upsideDown ? 8 : 0);
        int yDirection = (upsideDown ? -1 : 1);

        if (showHat && blendedHeadTextures.contains(skinLocation)) {
            if (drawShadow)
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getBlendedHeadLocation(skinLocation), x + 1, y, 0, yOffset, 8, 8, 8, yDirection * 8, 8, 8, shadowColor);

            // draw head in one draw call, fixing transparency issues of the "vanilla" path below
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getBlendedHeadLocation(skinLocation), x, y + shadowOffset, 0, yOffset, 8, 8, 8, yDirection * 8, 8, 8, color);
        } else {
            if (drawShadow) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skinLocation, x + 1, y,  8.0f, 8 + yOffset, 8, 8, 8, yDirection * 8, 64, 64, shadowColor);
                if (showHat) {
                    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skinLocation, x + 1, y, 40.0f, 8 + yOffset, 8, 8, 8, yDirection * 8, 64, 64, shadowColor);
                }
            }

            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skinLocation, x, y + shadowOffset,  8.0f, 8 + yOffset, 8, 8, 8, yDirection * 8, 64, 64, color);
            if (showHat) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skinLocation, x, y + shadowOffset, 40.0f, 8 + yOffset, 8, 8, 8, yDirection * 8, 64, 64, color);
            }
        }
    }

}
