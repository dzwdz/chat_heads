package dzwdz.chat_heads.mixin.render_targets;

import dzwdz.chat_heads.ChatHeads;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClothConfigScreen.class, remap = false)
public abstract class ClothConfigScreenMixin {
    @Inject(method = "render", at = @At("HEAD"), require = 0)
    public void chatheads$isConfigScreen(CallbackInfo ci) {
        ChatHeads.customHeadRendering = true;
    }

    @Inject(method = "render", at = @At("RETURN"), require = 0)
    public void chatheads$isOutsideConfigScreen(CallbackInfo ci) {
        ChatHeads.customHeadRendering = false;
    }
}
