package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.StandardChatListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(StandardChatListener.class)
public class StandardChatListenerMixin {
    @Inject(
            at = @At("HEAD"),
            method = "handle(Lnet/minecraft/network/chat/ChatType;Lnet/minecraft/network/chat/Component;Ljava/util/UUID;)V"
    )
    public void onChatMessage(ChatType messageType, Component message, UUID senderUuid, CallbackInfo callbackInfo) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();

        ChatHeads.lastSender = connection.getPlayerInfo(senderUuid);
        if (ChatHeads.lastSender != null)
            return;

        // check each word consisting only out of allowed player name characters
        for (String word : message.getString().split("(§.)|[^\\w]")) {
            if (word.isEmpty()) continue;

            // check if player name
            PlayerInfo player = connection.getPlayerInfo(word);
            if (player != null) {
                ChatHeads.lastSender = player;
                return;
            }

            // check if nickname
            for (PlayerInfo p : connection.getOnlinePlayers()) {
                // on vanilla servers this seems to always be null, apparently it can only be set via modifying
                // ServerPlayer.getTabListDisplayName() or sending an UPDATE_DISPLAY_NAME packet to the client
                // in other words, this is only for modded servers
                Component displayName = p.getTabListDisplayName();
                if (displayName != null && word.equals(displayName.getString())) {
                    ChatHeads.lastSender = p;
                    return;
                }
            }
        }
    }
}
