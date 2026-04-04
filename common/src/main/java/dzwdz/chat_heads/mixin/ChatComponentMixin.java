package dzwdz.chat_heads.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.HeadData;
import dzwdz.chat_heads.config.RenderPosition;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatComponent.class, priority = 990) // apply before Quark's ChatComponentMixin
public abstract class ChatComponentMixin {
    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I",
                    ordinal = 0
            ),
            index = 2
    )
    public int chatheads$moveText(Font font, FormattedCharSequence formattedCharSequence, int x, int y, int color,
            @Local GuiMessage.Line guiMessage, @Share("y") LocalIntRef yRef, @Share("opacity") LocalFloatRef opacityRef) {
        yRef.set(y);
        opacityRef.set((((color >> 24) + 256) % 256) / 255f); // haha yes
        return ChatHeads.getChatOffset(guiMessage);
    }

    @ModifyExpressionValue(method = "getTagIconLeft", at = @At(value = "CONSTANT", args = "intValue=4"))
    private int chatheads$moveTagIcon(int four, @Local(argsOnly = true) GuiMessage.Line guiMessage) {
        return four + ChatHeads.getTextWidthDifference(guiMessage);
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I",
                    ordinal = 0
            )
    )
    public void chatheads$renderChatHead(GuiGraphics guiGraphics, int i, int j, int k, CallbackInfo ci,
            @Local GuiMessage.Line guiMessage, @Share("y") LocalIntRef yRef, @Share("opacity") LocalFloatRef opacityRef) {
        HeadData headData = ChatHeads.getHeadData(guiMessage);
        if (headData == HeadData.EMPTY)
            return;

        if (ChatHeads.CONFIG.renderPosition() == RenderPosition.BEFORE_LINE) {
            ChatHeads.renderChatHead(guiGraphics, 0, yRef.get(), headData.playerInfo(), opacityRef.get());
        } else {
            // -> FontStringRenderOutputMixin
            ChatHeads.guiGraphics = guiGraphics;
            ChatHeads.renderHeadData = headData;
            ChatHeads.renderHeadOpacity = opacityRef.get();
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    public void chatheads$forgetRenderData(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, CallbackInfo ci,
                                           @Share("guiMessage") LocalRef<GuiMessage.Line> guiMessage, @Share("y") LocalIntRef yRef, @Share("opacity") LocalFloatRef opacityRef) {
        ChatHeads.guiGraphics = null;
        ChatHeads.renderHeadData = HeadData.EMPTY;
    }

    @ModifyArg(
            method = "getClickedComponentStyleAt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/StringSplitter;componentStyleAtWidth(Lnet/minecraft/util/FormattedCharSequence;I)Lnet/minecraft/network/chat/Style;"
            ),
            index = 1
    )
    public int chatheads$correctClickPosition(int x, @Local GuiMessage.Line guiMessage) {
        return x - ChatHeads.getTextWidthDifference(guiMessage);
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
        return original - ChatHeads.getTextWidthDifference(ChatHeads.getLineData());
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
