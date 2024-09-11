package dzwdz.chat_heads.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.HeadData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.StringRenderOutput.class)
public abstract class FontStringRenderOutputMixin {
    @Shadow float x;
    @Shadow float y;
    @Shadow @Final private Matrix4f pose;
    @Shadow @Final private boolean dropShadow;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void chatheads$resetCharacterCount(Font font, MultiBufferSource bufferSource, float x, float y, int color, boolean dropShadow, Matrix4f pose, Font.DisplayMode mode, int packedLightCoords, CallbackInfo ci) {
        // once for dropShadow = true, once for dropShadow = false
        ChatHeads.charsRendered = 0;
    }

    @Inject(method = "accept", at = @At("HEAD"))
    public void chatheads$renderChatHead(int i, Style style, int j, CallbackInfoReturnable<Boolean> cir) {
        if (ChatHeads.renderHeadData == HeadData.EMPTY)
            return;

        int renderIndex = Math.max(ChatHeads.renderHeadData.codePointIndex(), 0); // fallback to rendering at beginning

        if (ChatHeads.charsRendered == renderIndex) {
            if (!dropShadow) {
                PoseStack poseStack = ChatHeads.guiGraphics.pose();

                poseStack.pushPose();
                poseStack.setIdentity();
                poseStack.mulPoseMatrix(pose);

                ChatHeads.renderChatHead(ChatHeads.guiGraphics, (int) x + 1, (int) y, ChatHeads.renderHeadData.playerInfo(), ChatHeads.renderHeadOpacity);

                poseStack.popPose();
            }

            x += 8 + 2;
        }

        ChatHeads.charsRendered++;
    }
}
