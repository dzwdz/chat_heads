package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(
            at = @At("HEAD"),
            method = "debugWarn(Ljava/lang/String;[Ljava/lang/Object;)V"
    )
    public void debugWarnReset(CallbackInfo callbackInfo) {
        ChatHeads.lastSender = null;
    }

    @Inject(
            at = @At("HEAD"),
            method = "debugError(Ljava/lang/String;[Ljava/lang/Object;)V"
    )
    public void debugErrorReset(CallbackInfo callbackInfo) {
        ChatHeads.lastSender = null;
    }
}
