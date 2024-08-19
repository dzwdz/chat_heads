package dzwdz.chat_heads.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    public void f(CallbackInfoReturnable<Component> cir) {
//        cir.setReturnValue(Component.literal("\uD800\uDF00\uD800\uDF01\uD800\uDF01\uD800\uDF00"));
    }
}
