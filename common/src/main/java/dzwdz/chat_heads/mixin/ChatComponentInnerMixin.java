package dzwdz.chat_heads.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static dzwdz.chat_heads.config.RenderPosition.BEFORE_LINE;

@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$1")
public abstract class ChatComponentInnerMixin {
    @ModifyArgs(
            method = "accept",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;handleMessage(IFLnet/minecraft/util/FormattedCharSequence;)Z"
            )
    )
    private void chatheads$offsetChatMessage(Args args, @Local(argsOnly = true) GuiMessage.Line line,
            @Share("chatOffset") LocalIntRef chatOffset) {
        if (ChatHeads.CONFIG.renderPosition() == BEFORE_LINE) {
            chatOffset.set(ChatHeads.getChatOffset(line));

            ChatHeads.chatGraphicsAccess.updatePose((matrix3x2f) -> {
                matrix3x2f.translate(chatOffset.get(), 0);
            });
        }
    }

    @Inject(
            method = "accept",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;handleMessage(IFLnet/minecraft/util/FormattedCharSequence;)Z",
                    shift = At.Shift.AFTER
            )
    )
    private void chatheads$undoChatMessageOffset(CallbackInfo ci, @Share("chatOffset") LocalIntRef chatOffset) {
        if (ChatHeads.CONFIG.renderPosition() == BEFORE_LINE) {
            ChatHeads.chatGraphicsAccess.updatePose((matrix3x2f) -> {
                matrix3x2f.translate(-chatOffset.get(), 0);
            });
        }
    }

    @Inject(
            method = "accept",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;handleTagIcon(IIZLnet/minecraft/client/multiplayer/chat/GuiMessageTag;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag$Icon;)V"
            )
    )
    private void chatheads$offsetTagIcon(CallbackInfo ci, @Share("chatOffset") LocalIntRef chatOffset) {
        if (ChatHeads.CONFIG.renderPosition() == BEFORE_LINE) {
            ChatHeads.chatGraphicsAccess.updatePose((matrix3x2f) -> {
                matrix3x2f.translate(chatOffset.get(), 0);
            });
        }
    }

    @Inject(
            method = "accept",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;handleTagIcon(IIZLnet/minecraft/client/multiplayer/chat/GuiMessageTag;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag$Icon;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void chatheads$undoTagIconOffset(CallbackInfo ci, @Share("chatOffset") LocalIntRef chatOffset) {
        if (ChatHeads.CONFIG.renderPosition() == BEFORE_LINE) {
            ChatHeads.chatGraphicsAccess.updatePose((matrix3x2f) -> {
                matrix3x2f.translate(-chatOffset.get(), 0);
            });
        }
    }
}
