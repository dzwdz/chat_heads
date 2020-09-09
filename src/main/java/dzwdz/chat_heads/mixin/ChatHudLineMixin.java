package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.EntryPoint;
import dzwdz.chat_heads.mixinterface.ChatHudLineMixinAccessor;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.network.PlayerListEntry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHudLine.class)
public class ChatHudLineMixin implements ChatHudLineMixinAccessor {
    @Nullable
    public PlayerListEntry chatheads$owner;

    @Inject(
            at = @At("TAIL"),
            method = "<init>(ILjava/lang/Object;I)V"
    )
    public void init(CallbackInfo callbackInfo) {
        chatheads$owner = EntryPoint.lastSender;
    }

    @Override
    public PlayerListEntry chatheads$getOwner() {
        return chatheads$owner;
    }
}
