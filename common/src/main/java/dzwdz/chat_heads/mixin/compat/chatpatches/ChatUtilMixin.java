package dzwdz.chat_heads.mixin.compat.chatpatches;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.ComponentProcessor;
import dzwdz.chat_heads.HeadData;
import dzwdz.chat_heads.config.RenderPosition;
import net.minecraft.network.chat.Component;
import obro1961.chatpatches.util.ChatUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatUtil.class)
public abstract class ChatUtilMixin {
    // this is only for the chat history. the head for the actual message will be added at a later time, see ChatComponentMixin2
    @ModifyArg(method = "modifyMessage", at = @At(value = "INVOKE", target = "Lobro1961/chatpatches/ChatLog;addMessage(Lnet/minecraft/network/chat/Component;)V"), require = 0)
    private static Component chatheads$prependChatHead(Component message) {
        if (ChatHeads.CONFIG.renderPosition() == RenderPosition.BEFORE_LINE && ChatHeads.lastSenderData != HeadData.EMPTY) {
            return ComponentProcessor.prependChatHead(message, ChatHeads.lastSenderData.playerInfo());
        }

        return message;
    }
}
