package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.mixinterface.GuiMessageOwnerAccessor;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatComponent.class, priority = 10100) // apply after Compact Chat's potential refreshTrimmedMessage() and recursive addMessageToDisplayQueue() call
public abstract class ChatComponentMixin2 {
    @Inject(
            method = "addMessageToDisplayQueue(Lnet/minecraft/client/GuiMessage;)V",
            at = @At("HEAD")
    )
    private void chatheads$transferMessageOwner(GuiMessage guiMessage, CallbackInfo ci) {
        ChatHeads.lineOwner = ChatHeads.getOwner(guiMessage); // only really need to set when not refreshing
    }
}
