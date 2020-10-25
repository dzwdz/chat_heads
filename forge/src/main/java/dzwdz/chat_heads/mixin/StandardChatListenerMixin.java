package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NormalChatListener;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(NormalChatListener.class)
public class StandardChatListenerMixin {
    @Inject(
            at = @At("HEAD"),
            method = "Lnet/minecraft/client/gui/chat/NormalChatListener;handle(Lnet/minecraft/util/text/ChatType;Lnet/minecraft/util/text/ITextComponent;Ljava/util/UUID;)V"
    )
    public void onChatMessage(ChatType messageType, ITextComponent message, UUID senderUuid, CallbackInfo callbackInfo) {
        ChatHeads.lastSender = Minecraft.getInstance().getConnection().getPlayerInfo(senderUuid);
        String textString = message.getString();
        if (ChatHeads.lastSender == null) {
            for (String part : textString.split("(§.)|[^\\w]")) {
                if (part.isEmpty()) continue;
                NetworkPlayerInfo p = Minecraft.getInstance().getConnection().getPlayerInfo(part);
                if (p != null) {
                    ChatHeads.lastSender = p;
                    return;
                }
            }
        }
        for (NetworkPlayerInfo p: Minecraft.getInstance().getConnection().getOnlinePlayers()) {
            ITextComponent displayName = p.getTabListDisplayName();
            if (displayName != null && textString.contains(displayName.getString())) {
                ChatHeads.lastSender = p;
                return;
            }
        }
    }
}
