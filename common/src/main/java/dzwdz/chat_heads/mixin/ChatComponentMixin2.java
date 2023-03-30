package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatComponent.class, priority = 1010) // apply after Compact Chat's refreshTrimmedMessage() and recursive addMessage() call
public abstract class ChatComponentMixin2 {
    @Inject(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V",
            at = @At("HEAD")
    )
    private void addMessage(Component component, MessageSignature messageSignature, int i, GuiMessageTag guiMessageTag, boolean refreshing, CallbackInfo ci) {
        ChatHeads.refreshing = refreshing;
        ChatHeads.lineOwner = ChatHeads.lastSender; // only really need to set when not refreshing
    }
}
