package dzwdz.chat_heads.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.mixinterface.GuiMessageOwnerAccessor;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = ChatComponent.class, priority = 990) // apply before Quark's ChatComponentMixin
public abstract class ChatComponentMixin {
    @Shadow @Final private Minecraft minecraft;

    @ModifyVariable(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/GuiMessage$Line;addedTime()I"
            ),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;III)V"
    )
    public GuiMessage.Line chatheads$captureGuiMessage(GuiMessage.Line guiMessage) {
        ChatHeads.lastGuiMessage = guiMessage;
        ChatHeads.lastChatOffset = ChatHeads.getChatOffset(guiMessage);
        return guiMessage;
    }

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I",
                    ordinal = 0
            ),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;III)V",
            index = 2
    )
    public float chatheads$moveText(PoseStack poseStack, FormattedCharSequence formattedCharSequence, float x, float y, int color) {
        ChatHeads.lastY = (int) y;
        ChatHeads.lastOpacity = (((color >> 24) + 256) % 256) / 255f; // haha yes
        return ChatHeads.lastChatOffset;
    }

    @ModifyExpressionValue(method = "getTagIconLeft(Lnet/minecraft/client/GuiMessage$Line;)I", at = @At(value = "CONSTANT", args = "intValue=4"))
    private int chatheads$moveTagIcon(int four) {
        return four + ChatHeads.lastChatOffset;
    }

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I",
                    ordinal = 0
            ),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;III)V"
    )
    public void chatheads$renderChatHead(PoseStack matrixStack, int i, int j, int k, CallbackInfo ci) {
        PlayerInfo owner = ((GuiMessageOwnerAccessor) (Object) ChatHeads.lastGuiMessage).chatheads$getOwner();
        if (owner != null) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, ChatHeads.lastOpacity);
            ChatHeads.renderChatHead(matrixStack, 0, ChatHeads.lastY, owner);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
    }

    @ModifyVariable(
            at = @At("STORE"),
            method = "getClickedComponentStyleAt(DD)Lnet/minecraft/network/chat/Style;"
    )
    public GuiMessage.Line chatheads$updateChatOffset(GuiMessage.Line guiMessage) {
        ChatHeads.lastChatOffset = ChatHeads.getChatOffset(guiMessage);
        return guiMessage;
    }

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/StringSplitter;componentStyleAtWidth(Lnet/minecraft/util/FormattedCharSequence;I)Lnet/minecraft/network/chat/Style;"
            ),
            method = "getClickedComponentStyleAt(DD)Lnet/minecraft/network/chat/Style;",
            index = 1
    )
    public int chatheads$correctClickPosition(int x) {
        return x - ChatHeads.lastChatOffset;
    }

    @ModifyExpressionValue(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;getWidth()I"
            ),
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"
    )
    public int chatheads$fixTextOverflow(int original) {
        // at this point, neither lastGuiMessage nor lastChatOffset are well-defined
        return original - ChatHeads.getChatOffset(ChatHeads.getLineOwner());
    }

    // Compact Chat calls this at the beginning of addMessage (to get rid of old duplicate messages)
    @Inject(
            method = "refreshTrimmedMessage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void chatheads$transferMessageOwner(CallbackInfo ci, int i, GuiMessage guiMessage) {
        // transfer owner from GuiMessage to new GuiMessage.Line
        ChatHeads.refreshingLineOwner = ((GuiMessageOwnerAccessor) (Object) guiMessage).chatheads$getOwner();
    }
}
