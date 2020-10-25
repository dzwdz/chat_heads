package dzwdz.chat_heads.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.mixinterface.GuiMessageOwnerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.util.IReorderingProcessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NewChatGui.class)
public abstract class ChatComponentMixin {
    @Shadow @Final private Minecraft minecraft;

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/FontRenderer;drawShadow(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/util/IReorderingProcessor;FFI)I",
                    ordinal = 0
            ),
            method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;I)V",
            index = 2
    )
    public float moveTheText(MatrixStack poseStack, IReorderingProcessor formattedCharSequence, float f, float y, int color) {
        ChatHeads.lastY = (int)y;
        ChatHeads.lastOpacity = (((color >> 24) + 256) % 256) / 255f; // haha yes
        return ChatHeads.CHAT_OFFSET;
    }

    @ModifyVariable(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/ChatLine;getAddedTime()I"
            ),
            method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;I)V",
            print = true
    )
    public ChatLine<?> captureGuiMessage(ChatLine<?> guiMessage) {
        ChatHeads.lastGuiMessage = guiMessage;
        return guiMessage;
    }

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/FontRenderer;drawShadow(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/util/IReorderingProcessor;FFI)I",
                    ordinal = 0
            ),
            method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;I)V"
    )
    public void render(MatrixStack matrixStack, int i, CallbackInfo ci) {
        NetworkPlayerInfo owner = ((GuiMessageOwnerAccessor)ChatHeads.lastGuiMessage).chatheads$getOwner();
        if (owner != null) {
            RenderSystem.color4f(1, 1, 1, ChatHeads.lastOpacity);
            minecraft.getTextureManager().bind(owner.getSkinLocation());
            // draw base layer
            AbstractGui.blit(matrixStack, 0, ChatHeads.lastY, 8, 8, 8.0F, 8, 8, 8, 64, 64);
            // draw hat
            AbstractGui.blit(matrixStack, 0, ChatHeads.lastY, 8, 8, 40.0F, 8, 8, 8, 64, 64);
            RenderSystem.color4f(1, 1, 1, 1);
        }
    }

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/text/CharacterManager;componentStyleAtWidth(Lnet/minecraft/util/IReorderingProcessor;I)Lnet/minecraft/util/text/Style;"
            ),
            method = "getClickedComponentStyleAt(DD)Lnet/minecraft/util/text/Style;",
            index = 1
    )
    public int correctClickPosition(int x) {
        return x - ChatHeads.CHAT_OFFSET;
    }

    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/NewChatGui;getWidth()I"
            ),
            method = "addMessage(Lnet/minecraft/util/text/ITextComponent;IIZ)V"
    )
    public int fixTextOverflow(NewChatGui chatHud) {
        return NewChatGui.getWidth(minecraft.options.chatWidth) - ChatHeads.CHAT_OFFSET;
    }
}