package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.HeadData;
import dzwdz.chat_heads.mixininterface.HeadRenderable;
import net.minecraft.client.GuiMessage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMessage.class)
public abstract class GuiMessageMixin implements HeadRenderable {
    @Unique @NotNull
    public HeadData chatheads$headData = HeadData.EMPTY;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void chatheads$setOwnerOnFirst(CallbackInfo callbackInfo) {
        chatheads$headData = ChatHeads.lastSenderData;
        ChatHeads.lastSenderData = HeadData.EMPTY; // we're effectively at the end of a (non-refreshing) addMessage() call, good time as ever
    }

    @Override @NotNull
    public HeadData chatheads$getHeadData() {
        return chatheads$headData;
    }
}
