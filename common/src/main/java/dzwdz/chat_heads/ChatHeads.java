package dzwdz.chat_heads;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import dzwdz.chat_heads.config.ChatHeadsConfig;
import dzwdz.chat_heads.config.ChatHeadsConfigDefaults;
import dzwdz.chat_heads.config.ClothConfigCommonImpl;
import dzwdz.chat_heads.config.RenderPosition;
import dzwdz.chat_heads.mixininterface.HeadRenderable;
import dzwdz.chat_heads.mixininterface.Ownable;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static dzwdz.chat_heads.config.SenderDetection.HEURISTIC_ONLY;
import static dzwdz.chat_heads.config.SenderDetection.UUID_ONLY;
import static net.minecraft.network.chat.ClickEvent.Action.SUGGEST_COMMAND;

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
    public static List<String> modsIncompatibleWithBeforeName = List.of("caxton", "modernui");

    public static void init() {
        for (var modId : modsIncompatibleWithBeforeName) {
            if (Compat.isModLoaded(modId)) {
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

    public static int getChatOffset(@NotNull GuiMessage.Line guiMessage) {
        return getChatOffset(getHeadData(guiMessage));
    }

    public static int getChatOffset(@NotNull HeadData headData) {
        if (ChatHeads.CONFIG.renderPosition() != RenderPosition.BEFORE_LINE)
            return 0;

        if (headData != HeadData.EMPTY || (ChatHeads.CONFIG.offsetNonPlayerText() && !ChatHeads.serverDisabledChatHeads)) {
            return 8 + 2;
        } else {
            return 0;
        }
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
            ClickEvent clickEvent = style.getClickEvent();

            if (clickEvent != null && clickEvent.getAction() == SUGGEST_COMMAND) {
                String cmd = clickEvent.getValue();

                //noinspection ConstantValue  can apparently be null, see issue #112
                if (cmd != null && cmd.startsWith("/tell ")) {
                    String name = cmd.substring("/tell ".length()).trim();
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
        // vanilla skins are 64x64 pixels, HD skins (e.g. with CustomSkinLoader) 128x128
        int xScale = skin.getWidth() / 64;
        int yScale = skin.getHeight() / 64;

        NativeImage head = new NativeImage(8 * xScale, 8 * yScale, false);

        for (int y = 0; y < head.getHeight(); y++) {
            for (int x = 0; x < head.getWidth(); x++) {
                int headColor = skin.getPixelRGBA(8 * xScale + x, 8 * yScale + y);
                int hatColor = skin.getPixelRGBA(40 * xScale + x, 8 * yScale + y);

                // blend layers together
                head.setPixelRGBA(x, y, headColor);
                head.blendPixel(x, y, hatColor);
            }
        }

        return head;
    }

    public static ResourceLocation getBlendedHeadLocation(ResourceLocation skinLocation) {
        return ResourceLocation.fromNamespaceAndPath(ChatHeads.MOD_ID, skinLocation.getPath());
    }

    public static void renderChatHead(GuiGraphics guiGraphics, int x, int y, PlayerInfo owner, float opacity) {
        ResourceLocation skinLocation = owner.getSkin().texture();

        if (opacity != 1.0f) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);
        }

        if (blendedHeadTextures.contains(skinLocation)) {
            // draw head in one draw call, fixing transparency issues of the "vanilla" path below
            guiGraphics.blit(getBlendedHeadLocation(skinLocation), x, y, 8, 8, 0, 0, 8, 8, 8, 8);
        } else {
            // draw base layer
            guiGraphics.blit(skinLocation, x, y, 8, 8, 8.0f, 8, 8, 8, 64, 64);
            // draw hat
            guiGraphics.blit(skinLocation, x, y, 8, 8, 40.0f, 8, 8, 8, 64, 64);
        }

        if (opacity != 1.0f) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
    }
}
