package dzwdz.chat_heads.mixin;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(GameProfile.class)
public abstract class PlayerMixin2 {
    @Shadow @Final @Mutable
    private String name;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void illegalGameProfileName(UUID id, String name, CallbackInfo ci) {
//        this.name = "\uD800\uDF00\uD800\uDF01\uD800\uDF01\uD800\uDF00";
    }
}
