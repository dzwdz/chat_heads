package dzwdz.chat_heads.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dzwdz.chat_heads.mixininterface.TextureLocationSettable;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(SkinManager.TextureCache.class)
public abstract class SkinManagerMixin {
    // ModifyArgs crashes Forge
    @Inject(
            method = "registerTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/TextureManager;register(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V"
            )
    )
    public void chatheads$rememberTextureLocation(CallbackInfoReturnable<CompletableFuture<ResourceLocation>> cir, @Local ResourceLocation id, @Local HttpTexture httpTexture) {
        if (id.getPath().startsWith("skins/")) {
            ((TextureLocationSettable) httpTexture).chatheads$setTextureLocation(id);
        }
    }
}
