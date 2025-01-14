package dzwdz.chat_heads.forge.mixin.compat;

import dzwdz.chat_heads.ChatHeads;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.violetmoon.quark.content.management.module.ItemSharingModule;

import static dzwdz.chat_heads.config.RenderPosition.BEFORE_NAME;

@Mixin(ItemSharingModule.Client.class)
public abstract class QuarkItemSharingModuleMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private static float chatheads$fixOffset(float extraShift) {
        // assume a chat head is rendered before the item
        if (ChatHeads.CONFIG.renderPosition() == BEFORE_NAME)
            return extraShift + 8;

        return extraShift;
    }
}
