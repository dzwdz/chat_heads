package dzwdz.chat_heads.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.mixinterface.HttpTextureAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dzwdz.chat_heads.ChatHeads.extractBlendedHead;
import static dzwdz.chat_heads.ChatHeads.getBlendedHeadLocation;

// extract blended head and register as separate texture
// note that this won't work with OfflineSkins / SkinChanger since they use their own skin loading methods
@Mixin(HttpTexture.class)
public abstract class HttpTextureMixin implements HttpTextureAccessor {
    @Unique
    private ResourceLocation chatheads$textureLocation;

    public void chatheads$setTextureLocation(ResourceLocation textureLocation) {
        chatheads$textureLocation = textureLocation;
    }

    @Inject(method = "loadCallback", at = @At("HEAD"))
    public void chatheads$registerBlendedHeadTexture(NativeImage image, CallbackInfo ci) {
        // mods like Essential don't use SkinManager and textureLocation is thus never set, we hence simply disable texture blending
        if (chatheads$textureLocation == null) {
            return;
        }

        Minecraft.getInstance().getTextureManager()
                .register(getBlendedHeadLocation(chatheads$textureLocation), new DynamicTexture(extractBlendedHead(image)));

        ChatHeads.blendedHeadTextures.add(chatheads$textureLocation);
    }
}
