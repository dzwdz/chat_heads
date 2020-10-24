package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatListenerHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ChatListenerHud.class)
public class ChatListenerHudMixin {
    @Inject(
            at = @At("HEAD"),
            method = "onChatMessage(Lnet/minecraft/network/MessageType;Lnet/minecraft/text/Text;Ljava/util/UUID;)V"
    )
    public void onChatMessage(MessageType messageType, Text message, UUID senderUuid, CallbackInfo callbackInfo) {
        ChatHeads.lastSender = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(senderUuid);
        String textString = message.getString();
        if (ChatHeads.lastSender == null) {
            for (String part : textString.split("(§.)|[^\\w]")) {
                if (part.isEmpty()) continue;
                PlayerListEntry p = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(part);
                if (p != null) {
                    ChatHeads.lastSender = p;
                    return;
                }
            }
        }
        for (PlayerListEntry p: MinecraftClient.getInstance().getNetworkHandler().getPlayerList()) {
            Text displayName = p.getDisplayName();
            if (displayName != null && textString.contains(displayName.getString())) {
                ChatHeads.lastSender = p;
                return;
            }
        }
    }
}
