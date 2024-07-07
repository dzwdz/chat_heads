package dzwdz.chat_heads.mixin;

import com.mojang.authlib.GameProfile;
import dzwdz.chat_heads.ChatHeads;
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
    // called after message filtering, either from handlePlayerChatMessage directly, or after some potential chat delay
    @Inject(
        method = "showMessageToPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"
        )
    )
    public void chatheads$handleAddedPlayerMessage(ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component message, GameProfile gameProfile, boolean bl, Instant instant, CallbackInfoReturnable<Boolean> cir) {
        // it looks like gameProfile.getId() *could* be different from the sender UUID (or null), so we use the latter instead
        ChatHeads.handleAddedMessage(message, bound, ChatHeads.getOwner(playerChatMessage));
    }

    // handleDisguisedChatMessage: see project specific ChatListenerMixin

    // called for system messages
    @Inject(
            method = "handleSystemMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"
            )
    )
    public void chatheads$handleAddedSystemMessage(Component message, boolean bl, CallbackInfo ci) {
        if (ChatHeads.CONFIG.handleSystemMessages())
            ChatHeads.handleAddedMessage(message, null, null);
    }
}
