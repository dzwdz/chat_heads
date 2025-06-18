package dzwdz.chat_heads;

import com.mojang.blaze3d.platform.NativeImage;
import dzwdz.chat_heads.config.ChatHeadsConfig;
import dzwdz.chat_heads.config.ChatHeadsConfigDefaults;
import dzwdz.chat_heads.config.ClothConfigCommonImpl;
import dzwdz.chat_heads.mixininterface.HeadRenderable;
import dzwdz.chat_heads.mixininterface.Ownable;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.ClickEvent.SuggestCommand;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;

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
    public static final String FORMAT_REGEX = "§.";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final ResourceLocation DISABLE_RESOURCE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "disable");

    public static final int HEAD_WIDTH = 8 + 2; // pixels the head takes up (including padding)

    public static ChatHeadsConfig CONFIG = new ChatHeadsConfigDefaults();

    @NotNull public static HeadData lastSenderData = HeadData.EMPTY;

    // with Compact Chat, addMessageToDisplayQueue() calls refreshTrimmedMessage() and thus addMessageToDisplayQueue() with another owner inside itself,
    // we hence need two separate owner variables, distinguished by 'refreshing'
    public static boolean refreshing;
    @NotNull public static HeadData lineData = HeadData.EMPTY;
    @NotNull public static HeadData refreshingLineData = HeadData.EMPTY;

    public static volatile boolean serverSentUuid = false;
    public static volatile boolean serverDisabledChatHeads = false;

    public static final Set<ResourceLocation> blendedHeadTextures = new HashSet<>();

    // for "before name" rendering:
    public static GuiGraphics guiGraphics;
    @NotNull public static HeadData renderHeadData = HeadData.EMPTY;
    public static float renderHeadOpacity;

    public static boolean forceBeforeLine;
    private static final Map<String, BooleanSupplier> beforeNameIncompatibility = Map.of(
            "caxton", () -> true,
            "modernui", () -> {
                try {
                    // Emojiful makes Modern UI sort of compatible
                    if (Compat.isModLoaded("emojiful"))
                        return false;

                    Class<?> modernUi;
                    try {
                        modernUi = Class.forName("icyllis.modernui.mc.ModernUIMod");
                    } catch (ClassNotFoundException e) {
                        // old Forge versions
                        modernUi = Class.forName("icyllis.modernui.mc.forge.ModernUIForge");
                    }

                    return (Boolean) modernUi.getMethod("isTextEngineEnabled").invoke(null);
                } catch (Exception e) {
                    LOGGER.warn("couldn't invoke isTextEngineEnabled: {}: {}", e.getClass().getSimpleName(), e.getMessage());
                    return false;
                }
            }
    );

    public static void init() {
        for (var entry : beforeNameIncompatibility.entrySet()) {
            String modId = entry.getKey();

            if (Compat.isModLoaded(modId) && entry.getValue().getAsBoolean()) {
                forceBeforeLine = true;
                ChatHeads.LOGGER.warn("disabled \"Before Name\" rendermode due to incompatibility with {}", modId);
            }
        }

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

    public static void handleAddedMessage(Component message, @Nullable ChatType.Bound bound, @Nullable PlayerInfo playerInfo) {
        if (ChatHeads.serverDisabledChatHeads) {
            ChatHeads.lastSenderData = HeadData.EMPTY;
            return;
        }

        // note: while this may get us a head position, the message may be modified (e.g. by Chat Timestamps)
        // we hence update the position at the last possible moment, see chatheads$updateHeadPosition
        ChatHeads.lastSenderData = detectPlayer(message, bound, playerInfo);
    }

    @NotNull
    private static HeadData detectPlayer(Component message, @Nullable ChatType.Bound bound, @Nullable PlayerInfo playerInfo) {
        HeadData headData = detectShowcaseItemMessage(message);
        if (headData != null) return headData;

        if (ChatHeads.CONFIG.senderDetection() != HEURISTIC_ONLY) {
            if (playerInfo != null) {
                ChatHeads.serverSentUuid = true;
                return HeadData.of(playerInfo);
            }

            // no PlayerInfo/UUID, message is either not from a player or the server didn't wanna tell

            if (ChatHeads.CONFIG.senderDetection() == UUID_ONLY || ChatHeads.serverSentUuid && ChatHeads.CONFIG.smartHeuristics()) {
                return HeadData.EMPTY;
            }
        }

        return ChatHeads.detectPlayerByHeuristic(message, bound);
    }

    @Nullable
    private static HeadData detectShowcaseItemMessage(Component message) {
        if (message.getContents() instanceof TranslatableContents contents
                && Objects.equals(contents.getKey(), "showcaseitem.misc.shared_item")
                && contents.getArgs().length > 0) {
            var connection = Minecraft.getInstance().getConnection();
            if (connection == null)
                return null;

            String playerName;
            if (contents.getArgs()[0] instanceof String s) {
                playerName = s;
            } else if (contents.getArgs()[0] instanceof Component c) {
                playerName = c.getString();
            } else {
                return null;
            }

            var playerInfoCache = new PlayerInfoCache(connection);
            playerInfoCache.collectAllNames();

            return HeadData.of(playerInfoCache.get(playerName));
        }

        return null;
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
        return offsetChat(headData) ? HEAD_WIDTH : 0;
    }

    public static int getTextWidthDifference(@NotNull GuiMessage.Line guiMessage) {
        return getTextWidthDifference(getHeadData(guiMessage));
    }

    public static int getTextWidthDifference(@NotNull HeadData headData) {
        // whenever a head is rendered or chat is being offset
        return (headData != HeadData.EMPTY || offsetChat(headData)) ? HEAD_WIDTH : 0;
    }

    /** Heuristic to detect the sender of a message, needed if there's no sender UUID */
    @NotNull
    public static HeadData detectPlayerByHeuristic(Component message, @Nullable ChatType.Bound bound) {
        var connection = Minecraft.getInstance().getConnection();

        // When Polymer's early play networking API is used, messages can be received pre-login, in which case we disable chat heads
        if (connection == null) {
            return HeadData.EMPTY;
        }

        Component sender = getSenderDecoration(bound);

        var playerInfoCache = new PlayerInfoCache(connection);
        playerInfoCache.collectProfileNames();

        // StyledNicknames compatibility: try to get player info from /tell click event
        PlayerInfo player = getTellReceiver(sender != null ? sender : message).map(playerInfoCache::get).orElse(null);
        if (player != null) {
            return HeadData.of(player);
        }

        playerInfoCache.collectAllNames();

        // try to get player info only from the sender decoration
        if (sender != null) {
            String cleanSender = sender.getString().replaceAll(FORMAT_REGEX, "");
            return HeadData.of(playerInfoCache.get(cleanSender));
        } else {
            return scanForPlayerName(message.getString(), playerInfoCache);
        }
    }

    private static Optional<String> getTellReceiver(Component component) {
        return component.visit((style, string) -> {
            if (style.getClickEvent() instanceof SuggestCommand(String command)) {
                //noinspection ConstantValue  can apparently be null, see issue #112
                if (command != null && command.startsWith("/tell ")) {
                    String name = command.substring("/tell ".length()).trim();
                    return Optional.of(name);
                }
            }

            return Optional.empty();
        }, Style.EMPTY);
    }

    @Nullable
    private static Component getSenderDecoration(@Nullable ChatType.Bound bound) {
        if (bound == null) return null;

        for (var param : bound.chatType().value().chat().parameters()) {
            if (param == ChatTypeDecoration.Parameter.SENDER) {
                return bound.name();
            }
        }

        return null;
    }

    @NotNull
    public static HeadData scanForPlayerName(@NotNull String message, PlayerInfoCache playerInfoCache) {
        message = message.replaceAll(FORMAT_REGEX, "");

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
            String profileName = playerInfo.getProfile().getName().replaceAll(FORMAT_REGEX, "");
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
                String displayName = playerInfo.getTabListDisplayName().getString().replaceAll(FORMAT_REGEX, "");
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

    public static ResourceLocation getBlendedHeadLocation(ResourceLocation skinLocation) {
        return ResourceLocation.fromNamespaceAndPath(ChatHeads.MOD_ID, skinLocation.getPath());
    }

    public static void renderChatHead(GuiGraphics guiGraphics, int x, int y, PlayerInfo owner, float opacity) {
        ResourceLocation skinLocation = owner.getSkin().texture();

        int color = ARGB.white(opacity);

        if (blendedHeadTextures.contains(skinLocation)) {
            // draw head in one draw call, fixing transparency issues of the "vanilla" path below
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getBlendedHeadLocation(skinLocation), x, y, 0, 0, 8, 8, 8, 8, 8, 8, color);
        } else {
            // draw base layer
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skinLocation, x, y,  8.0f, 8, 8, 8, 8, 8, 64, 64, color);
            // draw hat
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skinLocation, x, y, 40.0f, 8, 8, 8, 8, 8, 64, 64, color);
        }
    }
}
