package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.mixininterface.VisibleInLog;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerSprite.class)
public class PlayerSpriteMixin implements VisibleInLog {
    @Unique
    boolean chatheads$visibleInLog;

    @Override
    public void chatheads$setVisibleInLog(boolean visibleInLog) {
        chatheads$visibleInLog = visibleInLog;
    }

    @Override
    public boolean chatheads$getVisibleInLog() {
        return chatheads$visibleInLog;
    }

    @Inject(method = "defaultFallback", at = @At("HEAD"), cancellable = true)
    public void chatheads$hideInLog(CallbackInfoReturnable<String> cir) {
        // only change this inside logChatMessage call as Chat Patches needs the original string representation
        if (!chatheads$visibleInLog && ChatHeads.insideLog)
            cir.setReturnValue("");
    }
}
