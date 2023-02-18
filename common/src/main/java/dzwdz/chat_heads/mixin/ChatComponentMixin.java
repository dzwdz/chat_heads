package dzwdz.chat_heads.mixin;

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
    public GuiMessage.Line captureGuiMessage(GuiMessage.Line guiMessage) {
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
    public float moveTheText(PoseStack poseStack, FormattedCharSequence formattedCharSequence, float x, float y, int color) {
        ChatHeads.lastY = (int) y;
        ChatHeads.lastOpacity = (((color >> 24) + 256) % 256) / 255f; // haha yes
        return ChatHeads.lastChatOffset;
    }

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I",
                    ordinal = 0
            ),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;III)V"
    )
    public void renderChatHead(PoseStack matrixStack, int i, int j, int k, CallbackInfo ci) {
        PlayerInfo owner = ((GuiMessageOwnerAccessor) (Object) ChatHeads.lastGuiMessage).chatheads$getOwner();
        if (owner != null) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, ChatHeads.lastOpacity);
            ChatHeads.renderChatHead(matrixStack, 0, ChatHeads.lastY, owner);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    @ModifyVariable(
            at = @At("STORE"),
            method = "getClickedComponentStyleAt(DD)Lnet/minecraft/network/chat/Style;"
    )
    public GuiMessage.Line updateChatOffset(GuiMessage.Line guiMessage) {
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
    public int correctClickPosition(int x) {
        return x - ChatHeads.lastChatOffset;
    }

    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;getWidth()I"
            ),
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"
    )
    public int fixTextOverflow(ChatComponent chatHud) {
        // at this point, lastSender is well-defined but neither lastGuiMessage nor lastChatOffset
        return ChatComponent.getWidth(minecraft.options.chatWidth().get()) - ChatHeads.getChatOffset(ChatHeads.lastSender);
    }
}