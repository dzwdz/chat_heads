package dzwdz.chat_heads.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dzwdz.chat_heads.EntryPoint;
import dzwdz.chat_heads.mixinterface.ChatHudLineMixinAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/OrderedText;FFI)I",
                    ordinal = 0
            ),
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;I)V",
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    public void render(MatrixStack matrixStack, int i, CallbackInfo ci, int j, int k, boolean bl, double d, int l, double e, double f, double g, double h, int m, int n, ChatHudLine chatHudLine, double p, int q, int r, int s, double t) {
        PlayerListEntry owner = ((ChatHudLineMixinAccessor)chatHudLine).chatheads$getOwner();
        if (owner != null) {
            RenderSystem.color4f(1, 1, 1, (float) (p * e));
            int y = (int) (t + h);
            client.getTextureManager().bindTexture(owner.getSkinTexture());
            // draw base layer
            DrawableHelper.drawTexture(matrixStack, 0, y, 8, 8, 8.0F, 8, 8, 8, 64, 64);
            // draw hat
            DrawableHelper.drawTexture(matrixStack, 0, y, 8, 8, 40.0F, 8, 8, 8, 64, 64);
            RenderSystem.color4f(1, 1, 1, 1);
        }
    }

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/OrderedText;FFI)I",
                    ordinal = 0
            ),
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;I)V",
            index = 2
    )
    public float moveTheText(float prevX) {
        return EntryPoint.CHAT_OFFSET;
    }

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/font/TextHandler;getStyleAt(Lnet/minecraft/text/OrderedText;I)Lnet/minecraft/text/Style;"
            ),
            method = "getText(DD)Lnet/minecraft/text/Style;",
            index = 1
    )
    public int correctClickPosition(int x) {
        return x - EntryPoint.CHAT_OFFSET;
    }

    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/ChatHud;getWidth()I"
            ),
            method = "addMessage(Lnet/minecraft/text/Text;IIZ)V"
    )
    public int fixTextOverflow(ChatHud chatHud) {
        return ChatHud.getWidth(client.options.chatWidth) - EntryPoint.CHAT_OFFSET;
    }
}
