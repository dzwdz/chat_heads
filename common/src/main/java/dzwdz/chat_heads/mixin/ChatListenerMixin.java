package dzwdz.chat_heads.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
    // called after message filtering, either from handlePlayerChatMessage directly, or after some potential chat delay
    @ModifyArg(
            method = "showMessageToPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
                    ordinal = 0
            )
    )
    public Component chatheads$handleAddedPlayerMessage(Component message, @Local(argsOnly = true) PlayerChatMessage playerChatMessage) {
        // it looks like gameProfile.getId() *could* be different from the sender UUID (or null), so we use the latter instead
        return ChatHeads.handleAddedMessage(message, ChatHeads.getOwner(playerChatMessage));
    }

    // handleDisguisedChatMessage: see project specific ChatListenerMixin

    // called for system messages
    @ModifyArg(
            method = "handleSystemMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V",
                    ordinal = 0
            )
    )
    public Component chatheads$handleAddedSystemMessage(Component message) {
        if (ChatHeads.CONFIG.handleSystemMessages())
            return ChatHeads.handleAddedMessage(message, null);

        return message;
    }
}
