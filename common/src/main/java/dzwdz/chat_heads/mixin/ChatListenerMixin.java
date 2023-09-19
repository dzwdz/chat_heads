package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
    // called from processPlayerChatMessage(), after filtering
    @Inject(
        method = "showMessageToPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"
        )
    )
    public void chatheads$handleAddedPlayerMessage(ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component message, PlayerInfo playerInfo, boolean bl, Instant instant, CallbackInfoReturnable<Boolean> cir) {
        ChatHeads.handleAddedMessage(message, bound, playerInfo);
    }

    // called for player messages without UUID
    @Inject(
        method = "processNonPlayerChatMessage",
        at = @At("HEAD")
    )
    public void chatheads$handleAddedSystemSignedPlayerMessage(ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component message, CallbackInfoReturnable<Boolean> cir) {
        ChatHeads.handleAddedMessage(message, bound, null);
    }

    // called for system messages
    @Inject(
            method = "handleSystemMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"
            )
    )
    public void chatheads$handleAddedPlayerMessage(Component message, boolean bl, CallbackInfo ci) {
        if (ChatHeads.CONFIG.handleSystemMessages())
            ChatHeads.handleAddedMessage(message, null, null);
    }
}
