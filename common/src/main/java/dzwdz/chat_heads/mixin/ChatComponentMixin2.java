package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatComponent.class, priority = 10100) // apply after Compact Chat's potential refreshTrimmedMessage() and recursive addMessageToDisplayQueue() call
public abstract class ChatComponentMixin2 {
    @Inject(
            method = "addMessageToDisplayQueue",
            at = @At("HEAD")
    )
    private void chatheads$transferMessageOwner(GuiMessage guiMessage, CallbackInfo ci) {
        ChatHeads.lineOwner = ChatHeads.getOwner(guiMessage); // only really need to set when not refreshing
    }

    @ModifyArg(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessageToDisplayQueue(Lnet/minecraft/client/GuiMessage;)V"
            )
    )
    private GuiMessage chatheads$setOwner(GuiMessage message) {
        ChatHeads.setOwner(message, ChatHeads.lastSender);
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
        ChatHeads.setOwner(message, ChatHeads.lastSender);
        return message;
    }

    @Inject(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("RETURN")
    )
    private void chatheads$forgetSender(CallbackInfo ci) {
        ChatHeads.lastSender = null;
    }
}
