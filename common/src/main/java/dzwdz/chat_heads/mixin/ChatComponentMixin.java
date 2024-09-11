package dzwdz.chat_heads.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.HeadData;
import dzwdz.chat_heads.config.RenderPosition;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatComponent.class, priority = 990) // apply before Quark's ChatComponentMixin
public abstract class ChatComponentMixin {
    @Unique
    int chatheads$chatOffset; // used in render and getTagIconLeft

    @ModifyVariable(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/GuiMessage$Line;addedTime()I"
            )
    )
    public GuiMessage.Line chatheads$captureGuiMessage(GuiMessage.Line guiMessage,
            @Share("guiMessage") LocalRef<GuiMessage.Line> guiMessageRef) {
        guiMessageRef.set(guiMessage);
        chatheads$chatOffset = ChatHeads.getChatOffset(guiMessage);
        return guiMessage;
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I",
                    ordinal = 0
            ),
            index = 2
    )
    public float chatheads$moveText(PoseStack poseStack, FormattedCharSequence formattedCharSequence, float x, float y, int color,
            @Share("y") LocalIntRef yRef, @Share("opacity") LocalFloatRef opacityRef) {
        yRef.set((int) y);
        opacityRef.set((((color >> 24) + 256) % 256) / 255f); // haha yes
        return chatheads$chatOffset;
    }

    @ModifyExpressionValue(method = "getTagIconLeft", at = @At(value = "CONSTANT", args = "intValue=4"))
    private int chatheads$moveTagIcon(int four) {
        return four + chatheads$chatOffset;
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I",
                    ordinal = 0
            )
    )
    public void chatheads$renderChatHeadBeforeLine(PoseStack matrixStack, int i, int j, int k, CallbackInfo ci,
            @Share("guiMessage") LocalRef<GuiMessage.Line> guiMessage, @Share("y") LocalIntRef yRef, @Share("opacity") LocalFloatRef opacityRef) {
        HeadData headData = ChatHeads.getHeadData(guiMessage.get());
        if (headData == HeadData.EMPTY)
            return;

        if (ChatHeads.CONFIG.renderPosition() == RenderPosition.BEFORE_LINE) {
            ChatHeads.renderChatHead(matrixStack, 0, yRef.get(), headData.playerInfo(), opacityRef.get());
        } else {
            // -> FontStringRenderOutputMixin
            ChatHeads.renderHeadData = headData;
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    public void chatheads$renderChatHeadBeforeName(PoseStack matrixStack, int tickCount, int mouseX, int mouseY, CallbackInfo ci,
            @Share("opacity") LocalFloatRef opacityRef) {
        if (ChatHeads.renderHeadData != HeadData.EMPTY) {
            matrixStack.pushPose();
            matrixStack.setIdentity();
            matrixStack.mulPoseMatrix(ChatHeads.renderHeadPose);

            ChatHeads.renderChatHead(matrixStack, ChatHeads.renderHeadX, ChatHeads.renderHeadY, ChatHeads.renderHeadData.playerInfo(), opacityRef.get());

            matrixStack.popPose();
        }

        ChatHeads.renderHeadData = HeadData.EMPTY;
    }

    @ModifyVariable(method = "getClickedComponentStyleAt", at = @At("STORE"))
    public GuiMessage.Line chatheads$updateChatOffset(GuiMessage.Line guiMessage, @Share("chatOffset") LocalIntRef chatOffsetRef) {
        chatOffsetRef.set(ChatHeads.getChatOffset(guiMessage));
        return guiMessage;
    }

    @ModifyArg(
            method = "getClickedComponentStyleAt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/StringSplitter;componentStyleAtWidth(Lnet/minecraft/util/FormattedCharSequence;I)Lnet/minecraft/network/chat/Style;"
            ),
            index = 1
    )
    public int chatheads$correctClickPosition(int x, @Share("chatOffset") LocalIntRef chatOffsetRef) {
        return x - chatOffsetRef.get();
    }

    @ModifyExpressionValue(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;getWidth()I"
            )
    )
    public int chatheads$fixTextOverflow(int original) {
        // at this point, neither guiMessage nor chatOffset are well-defined
        return original - ChatHeads.getChatOffset(ChatHeads.getLineData());
    }

    // Compact Chat calls this at the beginning of addMessage (to get rid of old duplicate messages)
    @Inject(
            method = "refreshTrimmedMessage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V")
    )
    private void chatheads$transferMessageOwner(CallbackInfo ci, @Local GuiMessage guiMessage) {
        // transfer owner from GuiMessage to new GuiMessage.Line
        ChatHeads.refreshingLineData = ChatHeads.getHeadData(guiMessage);
    }
}
