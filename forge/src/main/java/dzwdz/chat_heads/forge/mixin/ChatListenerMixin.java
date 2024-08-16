package dzwdz.chat_heads.forge.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
    // either called from handleDisguisedChatMessage directly, or after some potential chat delay
    @Inject(
        method = "m_244709_", // lambda inside handleDisguisedChatMessage
        at = @At("HEAD"),
        remap = false
    )
    public void chatheads$handleAddedDisguisedMessage(ChatType.Bound bound, Component undecoratedMessage, Instant instant, CallbackInfoReturnable<Boolean> cir) {
        ChatHeads.handleAddedMessage(undecoratedMessage, bound, null);
    }
}
