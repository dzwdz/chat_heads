package dzwdz.chat_heads.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Supplier;

import static dzwdz.chat_heads.ChatHeads.extractBlendedHead;
import static dzwdz.chat_heads.ChatHeads.getBlendedHeadLocation;

// extract blended head and register as separate texture
// note that this won't work with OfflineSkins / SkinChanger since they use their own skin loading methods
@Mixin(SkinTextureDownloader.class)
public abstract class SkinTextureDownloaderMixin {
    @ModifyArg(method = "registerTextureInManager", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static Supplier<ResourceLocation> chatheads$registerBlendedHeadTexture(Supplier<ResourceLocation> supplier, @Local(argsOnly = true) ResourceLocation textureLocation, @Local(argsOnly = true) NativeImage image) {
        return () -> {
            // runs on Render thread
            if (textureLocation.getPath().startsWith("skins/")) {
                Minecraft.getInstance().getTextureManager()
                        .register(getBlendedHeadLocation(textureLocation), new DynamicTexture(extractBlendedHead(image)));

                ChatHeads.blendedHeadTextures.add(textureLocation);
            }

            return supplier.get();
        };
    }
}
