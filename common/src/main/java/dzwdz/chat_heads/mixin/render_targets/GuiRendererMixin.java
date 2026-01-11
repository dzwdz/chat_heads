package dzwdz.chat_heads.mixin.render_targets;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Inject(method = "prepare", at = @At("HEAD"))
    public void chatheads$isInsideGui(CallbackInfo ci) {
        ChatHeads.customHeadRendering = true;
    }

    @Inject(method = "prepare", at = @At("RETURN"))
    public void chatheads$isOutsideGui(CallbackInfo ci) {
        ChatHeads.customHeadRendering = false;
    }
}
