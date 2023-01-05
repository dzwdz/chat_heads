package dzwdz.chat_heads;

import dzwdz.chat_heads.config.ChatHeadsConfig;
import dzwdz.chat_heads.config.ChatHeadsConfigDefaults;
import dzwdz.chat_heads.mixinterface.GuiMessageOwnerAccessor;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

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
    public static ChatHeadsConfig CONFIG = new ChatHeadsConfigDefaults();

    @Nullable
    public static PlayerInfo lastSender;
    @Nullable
    public static GuiMessage.Line lastGuiMessage;

    public static int lastY = 0;
    public static float lastOpacity = 0.0f;
    public static int lastChatOffset;
    public static boolean serverSentUuid = false;

    public static void handleAddedMessage(Component message, @Nullable PlayerInfo playerInfo) {
        if (ChatHeads.CONFIG.senderDetection() != HEURISTIC_ONLY) {
            ChatHeads.lastSender = playerInfo;

            if (playerInfo != null) {
                ChatHeads.serverSentUuid = true;
                return;
            }

            // no PlayerInfo/UUID, message is either not from a player or the server didn't wanna tell

            if (ChatHeads.CONFIG.senderDetection() == UUID_ONLY || ChatHeads.serverSentUuid && ChatHeads.CONFIG.smartHeuristics()) {
                return;
            }
        }

        // use heuristic to find sender
        ChatHeads.lastSender = ChatHeads.detectPlayer(message);
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
    public static PlayerInfo detectPlayer(Component message) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        Map<String, PlayerInfo> nicknameCache = new HashMap<>();

        // check each word consisting only out of allowed player name characters
        for (String word : message.getString().split("(§.)|[^\\w]")) {
            if (word.isEmpty()) continue;

            // manually translate nickname to profile name (needed for non-displayname nicknames)
            word = CONFIG.getProfileName(word);

            // check if player name
            PlayerInfo player = connection.getPlayerInfo(word);
            if (player != null) return player;

            // check if nickname
            player = getPlayerFromNickname(word, connection, nicknameCache);
            if (player != null) return player;
        }

        return null;
    }

    // helper method for detectPlayer using an (initially empty) cache to speed up subsequent calls
    // this cache will either be full or empty after this method returns
    @Nullable
    private static PlayerInfo getPlayerFromNickname(String word, ClientPacketListener connection, Map<String, PlayerInfo> nicknameCache) {
        if (nicknameCache.isEmpty()) {
            for (PlayerInfo p : connection.getOnlinePlayers()) {
                // on vanilla servers this seems to always be null, apparently it can only be set via modifying
                // ServerPlayer.getTabListDisplayName() or sending an UPDATE_DISPLAY_NAME packet to the client
                Component displayName = p.getTabListDisplayName();

                if (displayName != null) {
                    String nickname = displayName.getString();

                    // found match, we are done
                    if (word.equals(nickname)) {
                        nicknameCache.clear(); // make sure to not leave the cache in an incomplete state
                        return p;
                    }

                    // fill cache for subsequent calls
                    nicknameCache.put(nickname, p);
                }
            }
        } else {
            // use prepared cache
            return nicknameCache.get(word);
        }

        return null;
    }
}
