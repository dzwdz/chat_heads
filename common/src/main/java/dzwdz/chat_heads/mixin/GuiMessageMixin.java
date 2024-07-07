package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.mixininterface.Ownable;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMessage.class)
public abstract class GuiMessageMixin implements Ownable {
    @Unique @Nullable
    public PlayerInfo chatheads$owner;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void chatheads$setOwnerOnFirst(CallbackInfo callbackInfo) {
        chatheads$owner = ChatHeads.lastSender;
        ChatHeads.lastSender = null; // GuiMessage gets passed to where we need it
    }

    @Override
    public PlayerInfo chatheads$getOwner() {
        return chatheads$owner;
    }
}
