package dzwdz.chat_heads.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.mixinterface.GuiMessageOwnerAccessor;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
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
                    target = "Lnet/minecraft/client/GuiMessage;getAddedTime()I"
            ),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V"
    )
    public GuiMessage<?> captureGuiMessage(GuiMessage<?> guiMessage) {
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
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
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
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V"
    )
    public void renderChatHead(PoseStack matrixStack, int i, CallbackInfo ci) {
        PlayerInfo owner = ((GuiMessageOwnerAccessor) ChatHeads.lastGuiMessage).chatheads$getOwner();
        if (owner != null) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, ChatHeads.lastOpacity);
            RenderSystem.setShaderTexture(0, owner.getSkinLocation());
            // draw base layer
            GuiComponent.blit(matrixStack, 0, ChatHeads.lastY, 8, 8, 8.0f, 8, 8, 8, 64, 64);
            // draw hat
            GuiComponent.blit(matrixStack, 0, ChatHeads.lastY, 8, 8, 40.0f, 8, 8, 8, 64, 64);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    @ModifyVariable(
            at = @At("STORE"),
            method = "getClickedComponentStyleAt(DD)Lnet/minecraft/network/chat/Style;"
    )
    public GuiMessage<?> updateChatOffset(GuiMessage<?> guiMessage) {
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
            method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V"
    )
    public int fixTextOverflow(ChatComponent chatHud) {
        // at this point, lastSender is well-defined but neither lastGuiMessage nor lastChatOffset
        return ChatComponent.getWidth(minecraft.options.chatWidth) - ChatHeads.getChatOffset(ChatHeads.lastSender);
    }
}