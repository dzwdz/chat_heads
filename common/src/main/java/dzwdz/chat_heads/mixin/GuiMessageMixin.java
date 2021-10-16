package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.mixinterface.GuiMessageOwnerAccessor;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMessage.class)
public class GuiMessageMixin implements GuiMessageOwnerAccessor {
    @Nullable
    public PlayerInfo chatheads$owner;

    @Inject(
            at = @At("TAIL"),
            method = "<init>(ILjava/lang/Object;I)V"
    )
    public void init(CallbackInfo callbackInfo) {
        chatheads$owner = ChatHeads.lastSender;
        // reset sender early so that multi-line chats and non-player messages don't receive a chat head
        ChatHeads.lastSender = null;
    }

    @Override
    public PlayerInfo chatheads$getOwner() {
        return chatheads$owner;
    }
}
