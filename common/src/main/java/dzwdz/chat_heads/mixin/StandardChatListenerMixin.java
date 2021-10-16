package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.StandardChatListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
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
        ChatHeads.lastSender = Minecraft.getInstance().getConnection().getPlayerInfo(senderUuid);
        String textString = message.getString();
        if (ChatHeads.lastSender == null) {
            for (String part : textString.split("(§.)|[^\\w]")) {
                if (part.isEmpty()) continue;
                PlayerInfo p = Minecraft.getInstance().getConnection().getPlayerInfo(part);
                if (p != null) {
                    ChatHeads.lastSender = p;
                    return;
                }
            }
        }
        for (PlayerInfo p: Minecraft.getInstance().getConnection().getOnlinePlayers()) {
            Component displayName = p.getTabListDisplayName();
            if (displayName != null && textString.contains(displayName.getString())) {
                ChatHeads.lastSender = p;
                return;
            }
        }
    }
}
