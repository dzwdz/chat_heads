package dzwdz.chat_heads;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dzwdz.chat_heads.config.ChatHeadsConfig;
import dzwdz.chat_heads.config.ChatHeadsConfigDefaults;
import dzwdz.chat_heads.mixinterface.GuiMessageOwnerAccessor;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static dzwdz.chat_heads.config.SenderDetection.HEURISTIC_ONLY;
import static dzwdz.chat_heads.config.SenderDetection.UUID_ONLY;

/*
 * For 1.19.1 it goes
 *
 * ChatListener.handleChatMessage()
 *  -> ChatListener.processPlayerChatMessage()
 *  -> ChatListener.showMessageToPlayer()
 *  -> ChatComponent.addMessage()
 *  -> new GuiMessage.Line()
 *
 * For system signed player messages (messages without UUID)
 *
 * ChatListener.handleChatMessage()
 *   -> ChatListener.processNonPlayerChatMessage()
 *   -> ChatComponent.addMessage()
 *   -> new GuiMessage.Line()
 *
 * handleChatMessage() resolves the PlayerInfo from the MessageSigner's profile UUID.
 * This replaces the previous need for the ChatSender in StandardChatListener.
 * (StandardChatListener doesn't exist anymore and ChatSender seems to now only be used locally for verification.)
 * Previously, GuiMessage was used for the full and split lines, now the latter uses GuiMessage.Line.
 *
 * For "proper" system messages, ChatListener.handleSystemMessage() is called.
 * https://github.com/Oharass/FreedomChat/releases converts chat messages to system messages, so we handle those as well.
 */

public class ChatHeads {
    public static final String MOD_ID = "chat_heads";
    public static final String NON_NAME_REGEX = "(§.)|[^\\w]";

    public static ChatHeadsConfig CONFIG = new ChatHeadsConfigDefaults();

    @Nullable
    public static PlayerInfo lastSender;

    // with Compact Chat, addMessage() can call refreshTrimmedMessage() and thus addMessage() with another owner inside itself,
    // we hence need two separate owner variables, distinguished by 'refreshing'
    public static boolean refreshing;
    @Nullable public static PlayerInfo lineOwner;
    @Nullable public static PlayerInfo refreshingLineOwner;

    @Nullable
    public static GuiMessage.Line lastGuiMessage;

    public static int lastY = 0;
    public static float lastOpacity = 0.0f;
    public static int lastChatOffset;
    public static boolean serverSentUuid = false;

    public static final Set<ResourceLocation> blendedHeadTextures = new HashSet<>();

    public static PlayerInfo getLineOwner() {
        return refreshing ? refreshingLineOwner : lineOwner;
    }

    public static void resetLineOwner() {
        if (refreshing) {
            refreshingLineOwner = null;
        } else {
            lineOwner = null;
        }
    }

    public static void handleAddedMessage(Component message, @Nullable ChatType.Bound bound, @Nullable PlayerInfo playerInfo) {
        if (ChatHeads.CONFIG.senderDetection() != HEURISTIC_ONLY) {
            if (playerInfo != null) {
                ChatHeads.lastSender = playerInfo;
                ChatHeads.serverSentUuid = true;
                return;
            }

            // no PlayerInfo/UUID, message is either not from a player or the server didn't wanna tell

            if (ChatHeads.CONFIG.senderDetection() == UUID_ONLY || ChatHeads.serverSentUuid && ChatHeads.CONFIG.smartHeuristics()) {
                ChatHeads.lastSender = null;
                return;
            }
        }

        // use heuristic to find sender
        ChatHeads.lastSender = ChatHeads.detectPlayer(message, bound);
    }

    public static int getChatOffset(@NotNull GuiMessage.Line guiMessage) {
        PlayerInfo owner = ((GuiMessageOwnerAccessor) (Object) guiMessage).chatheads$getOwner();
        return getChatOffset(owner);
    }

    public static int getChatOffset(@Nullable PlayerInfo owner) {
        if (owner != null || ChatHeads.CONFIG.offsetNonPlayerText()) {
            return 10;
        } else {
            return 0;
        }
    }

    /** Heuristic to detect the sender of a message, needed if there's no sender UUID */
    @Nullable
    public static PlayerInfo detectPlayer(Component message, @Nullable ChatType.Bound bound) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();

        // When Polymer's early play networking API is used, messages can be received pre-login, in which case we disable chat heads
        if (connection == null) {
            return null;
        }

        Component sender = getSenderDecoration(bound);
        if (sender != null) {
            // try to get player info only from the sender decoration
            String cleanSender = sender.getString().replaceAll(NON_NAME_REGEX, "");

            return getPlayerInfo(cleanSender, connection, null, null);
        } else {
            Map<String, PlayerInfo> profileNameCache = new HashMap<>();
            Map<String, PlayerInfo> nicknameCache = new HashMap<>();

            // check each word of the message consisting only out of allowed player name characters
            for (String word : message.getString().split(NON_NAME_REGEX)) {
                if (word.isEmpty()) continue;

                PlayerInfo player = getPlayerInfo(word, connection, profileNameCache, nicknameCache);
                if (player != null) return player;
            }
        }

        return null;
    }

    @Nullable
    private static Component getSenderDecoration(@Nullable ChatType.Bound bound) {
        if (bound == null) return null;

        for (var param : bound.chatType().chat().parameters()) {
            if (param == ChatTypeDecoration.Parameter.SENDER) {
                return bound.name();
            }
        }

        return null;
    }

    @Nullable
    private static PlayerInfo getPlayerInfo(String name, ClientPacketListener connection, Map<String, PlayerInfo> profileNameCache, Map<String, PlayerInfo> nicknameCache) {
        // manually translate nickname to profile name (needed for non-displayname nicknames)
        name = CONFIG.getProfileName(name).replaceAll(NON_NAME_REGEX, "");

        // check if player name
        PlayerInfo player = getPlayerFromProfileName(name, connection, profileNameCache);
        if (player != null) return player;

        // check if nickname
        return getPlayerFromNickname(name, connection, nicknameCache);
    }

    /**
     * Finds a value v in `collection` such that `keyFunction(v)` equals `key`.
     * Uses an (initially empty) cache to speed up subsequent calls.
     * This cache will either be full or empty after this method returns.
     */
    public static <V, K> V findByKey(Iterable<V> collection, Function<V, K> keyFunction, K key, @Nullable Map<K, V> cache) {
        if (cache != null && !cache.isEmpty()) {
            return cache.get(key);
        } else {
            for (V v : collection) {
                K k = keyFunction.apply(v);

                if (k != null) {
                    if (key.equals(k)) {
                        if (cache != null) cache.clear(); // make sure to not leave the cache in an incomplete state
                        return v;
                    }

                    // fill cache for subsequent calls
                    if (cache != null) cache.put(k, v);
                }
            }

            return null;
        }
    }

    // plugins like HaoNick can change the profile names to contain illegal characters like formatting codes, so we can't simply use connection.getPlayerInfo()
    public static PlayerInfo getPlayerFromProfileName(String word, ClientPacketListener connection, Map<String, PlayerInfo> profileNameCache) {
        return findByKey(connection.getOnlinePlayers(),
                playerInfo -> playerInfo.getProfile().getName().replaceAll(NON_NAME_REGEX, ""),
                word,
                profileNameCache);
    }

    @Nullable
    private static PlayerInfo getPlayerFromNickname(String word, ClientPacketListener connection, Map<String, PlayerInfo> nicknameCache) {
        return findByKey(connection.getOnlinePlayers(),
                playerInfo -> {
                    Component displayName = playerInfo.getTabListDisplayName();
                    return displayName != null ? displayName.getString().replaceAll(NON_NAME_REGEX, "") :  null;
                },
                word,
                nicknameCache);
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
        return new ResourceLocation(ChatHeads.MOD_ID, skinLocation.getPath());
    }

    public static void renderChatHead(PoseStack matrixStack, int x, int y, PlayerInfo owner) {
        ResourceLocation skinLocation = owner.getSkinLocation();

        if (blendedHeadTextures.contains(skinLocation)) {
            RenderSystem.setShaderTexture(0, getBlendedHeadLocation(skinLocation));

            // draw head in one draw call, fixing transparency issues of the "vanilla" path below
            GuiComponent.blit(matrixStack, x, y, 8, 8, 0, 0, 8, 8, 8, 8);
        } else {
            RenderSystem.setShaderTexture(0, skinLocation);

            // draw base layer
            GuiComponent.blit(matrixStack, x, y, 8, 8, 8.0f, 8, 8, 8, 64, 64);
            // draw hat
            GuiComponent.blit(matrixStack, x, y, 8, 8, 40.0f, 8, 8, 8, 64, 64);
        }
    }
}
