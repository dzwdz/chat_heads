package dzwdz.chat_heads;

import dzwdz.chat_heads.config.ChatHeadsConfig;
import dzwdz.chat_heads.config.ChatHeadsConfigDefaults;
import dzwdz.chat_heads.mixinterface.GuiMessageOwnerAccessor;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ChatHeads {
    public static final String MOD_ID = "chat_heads";
    public static ChatHeadsConfig CONFIG = new ChatHeadsConfigDefaults();

    @Nullable
    public static PlayerInfo lastSender;
    @Nullable
    public static GuiMessage<?> lastGuiMessage;

    public static int lastY = 0;
    public static float lastOpacity = 0.0f;
    public static int lastChatOffset;
    public static boolean serverSentUuid = false;

    public static int getChatOffset(@NotNull GuiMessage<?> guiMessage) {
        PlayerInfo owner = ((GuiMessageOwnerAccessor) guiMessage).chatheads$getOwner();
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
    public static PlayerInfo detectPlayer(ClientPacketListener connection, Component message) {
        Map<String, PlayerInfo> nicknameCache = new HashMap<>();

        // check each word consisting only out of allowed player name characters
        for (String word : message.getString().split("(§.)|[^\\w]")) {
            if (word.isEmpty()) continue;

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
