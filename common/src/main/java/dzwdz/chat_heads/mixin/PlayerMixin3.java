package dzwdz.chat_heads.mixin;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public abstract class PlayerMixin3 {
    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    public void f(CallbackInfoReturnable<Component> cir) {
//        cir.setReturnValue(Component.literal("\uD800\uDF00\uD800\uDF01\uD800\uDF01\uD800\uDF00"));
    }
}
