package dzwdz.chat_heads.forge.mixin.compat;

import dzwdz.chat_heads.ChatHeads;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static dzwdz.chat_heads.config.RenderPosition.BEFORE_NAME;

@Pseudo
@Mixin(targets = "com.ultramega.showcaseitem.ShowcaseItemFeature")
public abstract class ShowcaseItemFeatureMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private static float chatheads$fixOffset(float extraShift) {
        // assume a chat head is rendered before the item
        if (ChatHeads.CONFIG.renderPosition() == BEFORE_NAME)
            return extraShift + 8 + 2;

        return extraShift;
    }
}
