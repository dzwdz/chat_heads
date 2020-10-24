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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I",
                    ordinal = 0
            ),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    public void render(PoseStack matrixStack, int i, CallbackInfo ci, int j, int k, boolean bl, double d, int l, double e, double f, double g, double h, int m, int n, GuiMessage<?> chatHudLine, double p, int q, int r, int s, double t) {
        PlayerInfo owner = ((GuiMessageOwnerAccessor)chatHudLine).chatheads$getOwner();
        if (owner != null) {
            RenderSystem.color4f(1, 1, 1, (float) (p * e));
            int y = (int) (t + h);
            minecraft.getTextureManager().bind(owner.getSkinLocation());
            // draw base layer
            GuiComponent.blit(matrixStack, 0, y, 8, 8, 8.0F, 8, 8, 8, 64, 64);
            // draw hat
            GuiComponent.blit(matrixStack, 0, y, 8, 8, 40.0F, 8, 8, 8, 64, 64);
            RenderSystem.color4f(1, 1, 1, 1);
        }
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
    public float moveTheText(float prevX) {
        return ChatHeads.CHAT_OFFSET;
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
        return x - ChatHeads.CHAT_OFFSET;
    }

    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;getWidth()I"
            ),
            method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V"
    )
    public int fixTextOverflow(ChatComponent chatHud) {
        return ChatComponent.getWidth(minecraft.options.chatWidth) - ChatHeads.CHAT_OFFSET;
    }
}
