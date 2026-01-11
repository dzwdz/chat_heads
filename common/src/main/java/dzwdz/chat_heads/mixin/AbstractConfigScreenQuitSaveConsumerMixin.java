package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.config.ThreeDeeNessGuiProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// hack, don't look!
@Mixin(targets = "me.shedaniel.clothconfig2.gui.AbstractConfigScreen$QuitSaveConsumer", remap = false)
public abstract class AbstractConfigScreenQuitSaveConsumerMixin {
    @Inject(method = "accept", at = @At("HEAD"), require = 0)
    public void chatheads$resetConfig(boolean discard, CallbackInfo ci) {
        if (discard) {
            // reset temporary value
            ChatHeads.CONFIG.setThreeDeeNess(ThreeDeeNessGuiProvider.lastSavedValue);
        }
    }
}
