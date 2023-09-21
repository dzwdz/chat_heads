package dzwdz.chat_heads.mixin;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import dzwdz.chat_heads.mixinterface.HttpTextureAccessor;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;


@Mixin(SkinManager.TextureCache.class)
public abstract class SkinManagerMixin {
    @Inject(
            method = "registerTexture(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;)Ljava/util/concurrent/CompletableFuture;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/TextureManager;register(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void chatheads$rememberTextureLocation(MinecraftProfileTexture minecraftProfileTexture, CallbackInfoReturnable<CompletableFuture<ResourceLocation>> cir, String string, ResourceLocation id, Path path, CompletableFuture<?> completableFuture, HttpTexture httpTexture) {
        ((HttpTextureAccessor) httpTexture).chatheads$setTextureLocation(id);
    }
}
