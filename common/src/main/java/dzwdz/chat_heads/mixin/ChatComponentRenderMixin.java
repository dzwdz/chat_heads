package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatComponent.class)
public abstract class ChatComponentRenderMixin {
    @Inject(method = "render(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IIZ)V", at = @At("HEAD"))
    public void chatheads$isInsideChat(CallbackInfo ci) {
        ChatHeads.insideChat = true;
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IIZ)V", at = @At("RETURN"))
    public void chatheads$isOutsideChat(CallbackInfo ci) {
        ChatHeads.insideChat = false;
    }
}
