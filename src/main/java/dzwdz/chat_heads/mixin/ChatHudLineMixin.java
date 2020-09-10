package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.EntryPoint;
import dzwdz.chat_heads.mixinterface.ChatHudLineMixinAccessor;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.StringRenderable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHudLine.class)
public class ChatHudLineMixin implements ChatHudLineMixinAccessor {
    @Shadow @Final private StringRenderable text;
    @Nullable
    public PlayerListEntry chatheads$owner;

    @Inject(
            at = @At("TAIL"),
            method = "<init>(ILnet/minecraft/text/StringRenderable;I)V"
    )
    public void init(CallbackInfo callbackInfo) {
        chatheads$owner = EntryPoint.lastSender;
    }

    @Override
    public PlayerListEntry chatheads$getOwner() {
        return chatheads$owner;
    }
}
