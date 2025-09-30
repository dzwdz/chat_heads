package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.HeadData;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// needs to apply after Compact Chat's refreshTrimmedMessage() and recursive addMessage() call
// also after Chat Timestamps so the head position is correct
@Mixin(value = ChatComponent.class, priority = 10100)
public abstract class ChatComponentMixin2 {
    @Inject(
            method = "addMessageToDisplayQueue",
            at = @At("HEAD")
    )
    private void chatheads$transferMessageOwner(GuiMessage guiMessage, CallbackInfo ci) {
        ChatHeads.lineData = ChatHeads.lastSenderData; // only really need to set when not refreshing
    }

    @ModifyArg(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessageToDisplayQueue(Lnet/minecraft/client/GuiMessage;)V"
            )
    )
    private GuiMessage chatheads$setOwner(GuiMessage message) {
        ChatHeads.setHeadData(message, ChatHeads.lastSenderData);
        return message;
    }

    // just in case the GuiMessage isn't the same
    @ModifyArg(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessageToQueue(Lnet/minecraft/client/GuiMessage;)V"
            )
    )
    private GuiMessage chatheads$setOwner2(GuiMessage message) {
        ChatHeads.setHeadData(message, ChatHeads.lastSenderData);
        return message;
    }

    @Inject(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("RETURN")
    )
    private void chatheads$forgetSender(CallbackInfo ci) {
        ChatHeads.lastSenderData = HeadData.EMPTY;
    }
}
