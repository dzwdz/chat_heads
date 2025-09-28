package dzwdz.chat_heads.fabric.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
    // either called from handleDisguisedChatMessage directly, or after some potential chat delay
    @ModifyArg(
            method = "method_45745", // lambda inside handleDisguisedChatMessage
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V",
                    ordinal = 0
            )
    )
    public Component chatheads$handleAddedDisguisedMessage(Component message) {
        return ChatHeads.handleAddedMessage(message, null);
    }
}
