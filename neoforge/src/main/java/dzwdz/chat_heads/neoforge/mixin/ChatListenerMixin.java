package dzwdz.chat_heads.neoforge.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.BooleanSupplier;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
    // either called from handleDisguisedChatMessage directly, or after some potential chat delay
    @ModifyArg(
            method = "handleDisguisedChatMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/chat/ChatListener;handleMessage(Lnet/minecraft/network/chat/MessageSignature;Ljava/util/function/BooleanSupplier;)V"
            ),
            index = 1
    )
    public BooleanSupplier chatheads$handleAddedDisguisedMessage(BooleanSupplier original, @Local(argsOnly = true) Component undecoratedMessage, @Local(argsOnly = true) ChatType.Bound bound) {
        return () -> {
            ChatHeads.handleAddedMessage(undecoratedMessage, bound, null);
            return original.getAsBoolean();
        };
    }
}
