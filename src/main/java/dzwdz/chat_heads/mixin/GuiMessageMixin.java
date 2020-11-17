package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dzwdz.chat_heads.mixinterface.GuiMessageOwnerAccessor;

@Mixin(ChatLine.class)
public class GuiMessageMixin implements GuiMessageOwnerAccessor {
    public NetworkPlayerInfo chatheads$owner;

    @Inject(
            at = @At("TAIL"),
            method = "<init>(ILjava/lang/Object;I)V"
    )
    public void init(CallbackInfo callbackInfo) {
        chatheads$owner = ChatHeads.lastSender;
    }

    @Override
    public NetworkPlayerInfo chatheads$getOwner() {
        return chatheads$owner;
    }
}
