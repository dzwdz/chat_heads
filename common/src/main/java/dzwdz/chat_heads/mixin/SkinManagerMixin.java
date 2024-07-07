package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.mixininterface.TextureLocationSettable;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(SkinManager.TextureCache.class)
public abstract class SkinManagerMixin {
    @ModifyArgs(
            method = "registerTexture(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;)Ljava/util/concurrent/CompletableFuture;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/TextureManager;register(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V"
            )
    )
    public void chatheads$rememberTextureLocation(Args args) {
        ResourceLocation id = args.get(0);
        HttpTexture httpTexture = args.get(1);

        if (id.getPath().startsWith("skins/")) {
            ((TextureLocationSettable) httpTexture).chatheads$setTextureLocation(id);
        }
    }
}
