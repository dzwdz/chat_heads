package dzwdz.chat_heads.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.font.GlyphInfo;
import dzwdz.chat_heads.PaddedChatGlyph;
import net.minecraft.client.gui.font.PlayerGlyphProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PlayerGlyphProvider.class, priority = 500)
public abstract class PlayerGlyphProviderMixin {
    @ModifyExpressionValue(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/font/GlyphInfo;simple(F)Lcom/mojang/blaze3d/font/GlyphInfo;"
            )
    )
    private static GlyphInfo chatheads$addPaddingInsideChat(GlyphInfo original) {
        return new PaddedChatGlyph(original);
    }
}
