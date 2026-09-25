package dzwdz.chat_heads.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dzwdz.chat_heads.ChatHeads;
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

@Mixin(value = ChatComponent.class, priority = 990)
public abstract class ChatComponentMixin {
    @ModifyArg(
            method = "method_71991", // oh no... let's hope this doesn't break too often
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)V",
                    ordinal = 0
            ),
            index = 2
    )
    public int chatheads$moveText(Font font, FormattedCharSequence formattedCharSequence, int x, int y, int color,
            @Local(argsOnly = true) GuiGraphics guiGraphics, @Local(argsOnly = true) GuiMessage.Line guiMessage) {
        return x + ChatHeads.getChatOffset(guiMessage);
    }

    @ModifyExpressionValue(method = "getTagIconLeft", at = @At(value = "CONSTANT", args = "intValue=4"))
    private int chatheads$moveTagIcon(int four, @Local(argsOnly = true) GuiMessage.Line guiMessage) {
        return four + ChatHeads.getTextWidthDifference();
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
        return x - ChatHeads.getTextWidthDifference();
    }

    @ModifyExpressionValue(
            method = "addMessageToDisplayQueue",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;getWidth()I"
            )
    )
    public int chatheads$fixTextOverflow(int original) {
        return original - ChatHeads.getTextWidthDifference();
    }

    @Inject(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessageToDisplayQueue(Lnet/minecraft/client/GuiMessage;)V"
            )
    )
    private void chatheads$nonRefreshingPath(CallbackInfo ci) {
        ChatHeads.refreshing = false;
    }

    // Compact Chat calls this at the beginning of addMessageToDisplayQueue (to get rid of old duplicate messages)
    @ModifyArg(
            method = "refreshTrimmedMessages",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessageToDisplayQueue(Lnet/minecraft/client/GuiMessage;)V"
            )
    )
    private GuiMessage chatheads$transferMessageOwner(GuiMessage guiMessage) {
        // transfer owner from this GuiMessage to new GuiMessage.Line
        ChatHeads.refreshing = true;
        ChatHeads.refreshingLineData = ChatHeads.getHeadData(guiMessage);
        return guiMessage;
    }

    // Compact Chat calls this at the beginning of addMessageToDisplayQueue (to get rid of old duplicate messages)
    @Inject(
            method = "refreshTrimmedMessages",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessageToDisplayQueue(Lnet/minecraft/client/GuiMessage;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void chatheads$finishedRefreshing(CallbackInfo ci) {
        ChatHeads.refreshing = false;
    }
}
