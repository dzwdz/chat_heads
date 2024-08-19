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

@Mixin(GuiMessage.Line.class)
public abstract class GuiMessageLineMixin implements HeadRenderable {
    @Unique @NotNull
    public HeadData chatheads$headData = HeadData.EMPTY;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void chatheads$setOwnerForFirstLine(CallbackInfo callbackInfo) {
        chatheads$headData = ChatHeads.getLineData();
        ChatHeads.resetLineOwner(); // reset early so multi-line chats don't each receive a chat head
    }

    @Override @NotNull
    public HeadData chatheads$getHeadData() {
        return chatheads$headData;
    }
}
