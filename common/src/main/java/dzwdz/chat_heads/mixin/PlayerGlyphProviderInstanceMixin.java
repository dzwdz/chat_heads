package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static dzwdz.chat_heads.ChatHeads.CONFIG;

// implements 3Dness. these methods are called from GuiRenderer.render()
@Mixin(targets = "net.minecraft.client.gui.font.PlayerGlyphProvider$Instance")
public abstract class PlayerGlyphProviderInstanceMixin {
    // move head to the right
    @ModifyArg(method = "renderSprite",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/font/PlayerGlyphProvider$Instance;renderQuad(Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IFFFFFIFFIIII)V",
                    ordinal = 0
            ),
            index = 3
    )
    public float leftHead(float original) {
        if (!ChatHeads.customHeadRendering)
            return original;

        return original + CONFIG.threeDeeNess();
    }

    // move head to the right
    @ModifyArg(method = "renderSprite",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/font/PlayerGlyphProvider$Instance;renderQuad(Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IFFFFFIFFIIII)V",
                    ordinal = 0
            ),
            index = 4
    )
    public float rightHead(float original) {
        if (!ChatHeads.customHeadRendering)
            return original;

        return original + CONFIG.threeDeeNess();
    }

    // scale hat wider
    @ModifyArg(method = "renderSprite",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/font/PlayerGlyphProvider$Instance;renderQuad(Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IFFFFFIFFIIII)V",
                    ordinal = 1
            ),
            index = 4
    )
    public float rightHat(float original) {
        if (!ChatHeads.customHeadRendering)
            return original;

        return original + 2 * CONFIG.threeDeeNess();
    }

    // move hat up
    @ModifyArg(method = "renderSprite",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/font/PlayerGlyphProvider$Instance;renderQuad(Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IFFFFFIFFIIII)V",
                    ordinal = 1
            ),
            index = 5
    )
    public float topHat(float original) {
        if (!ChatHeads.customHeadRendering)
            return original;

        return original - CONFIG.threeDeeNess();
    }

    // scale hat taller
    @ModifyArg(method = "renderSprite",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/font/PlayerGlyphProvider$Instance;renderQuad(Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IFFFFFIFFIIII)V",
                    ordinal = 1
            ),
            index = 6
    )
    public float bottomHat(float original) {
        if (!ChatHeads.customHeadRendering)
            return original;

        return original + 2 * CONFIG.threeDeeNess();
    }
}
