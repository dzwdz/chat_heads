package dzwdz.chat_heads.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ChatComponent.ChatGraphicsAccess;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatComponent.class, priority = 990)
public abstract class ChatComponentMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V", at = @At("HEAD"))
    private static void chatheads$captureGuiGraphics(CallbackInfo ci, @Local(argsOnly = true) GuiGraphicsExtractor guiGraphics) {
        ChatHeads.guiGraphics = guiGraphics;
    }

    @Inject(method = "captureClickableText", at = @At("HEAD"))
    private static void chatheads$noGraphics(CallbackInfo ci) {
        ChatHeads.guiGraphics = null;
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V", at = @At("HEAD"))
    private static void chatheads$captureChatGraphicsAccess(CallbackInfo ci, @Local(argsOnly = true) ChatGraphicsAccess chatGraphicsAccess) {
        ChatHeads.chatGraphicsAccess = chatGraphicsAccess;
    }

    @ModifyArg(
            method = "lambda$extractRenderState$1", // render: forEachLine(alphaCalculator, lambda)
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;fill(IIIII)V"
            ),
            index = 2
    )
    private static int chatheads$fixTextOverflow(int original) {
        return original + ChatHeads.getTextWidthDifference(ChatHeads.getLineData());
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V", at = @At("RETURN"))
    private static void chatheads$forgetGraphics(CallbackInfo ci) {
        ChatHeads.guiGraphics = null;
        ChatHeads.chatGraphicsAccess = null;
    }

    @Inject(
            method = "addMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessageToDisplayQueue(Lnet/minecraft/client/multiplayer/chat/GuiMessage;)V"
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
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessageToDisplayQueue(Lnet/minecraft/client/multiplayer/chat/GuiMessage;)V"
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
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessageToDisplayQueue(Lnet/minecraft/client/multiplayer/chat/GuiMessage;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void chatheads$finishedRefreshing(CallbackInfo ci) {
        ChatHeads.refreshing = false;
    }
}
