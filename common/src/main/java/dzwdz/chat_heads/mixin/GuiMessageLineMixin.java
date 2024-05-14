package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.mixinterface.GuiMessageOwnerAccessor;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMessage.Line.class)
public abstract class GuiMessageLineMixin implements GuiMessageOwnerAccessor {
    @Unique @Nullable
    public PlayerInfo chatheads$owner;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void chatheads$setOwner(CallbackInfo callbackInfo) {
        chatheads$owner = ChatHeads.getLineOwner();
        ChatHeads.resetLineOwner(); // reset early so multi-line chats don't each receive a chat head
    }

    @Override
    public PlayerInfo chatheads$getOwner() {
        return chatheads$owner;
    }
}
