package dzwdz.chat_heads.mixin.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.HeadData;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// essentially a copy of FontStringRenderOutputMixin
@Mixin(targets = "com.hrznstudio.emojiful.render.EmojiFontRenderer$EmojiCharacterRenderer", remap = false)
public abstract class EmojifulMixin {
    @Shadow
    private float x;
    @Shadow @Final
    private float y;
    @Shadow @Final
    private Matrix4f matrix;
    @Shadow @Final private boolean dropShadow;

    @Unique
    private int chatheads$charsRendered = 0;

    @Inject(method = "accept", at = @At("HEAD"), require = 0, remap = true)
    public void f(int pos, Style style, int charInt, CallbackInfoReturnable<Boolean> cir) {
        if (ChatHeads.renderHeadData == HeadData.EMPTY)
            return;

        int renderIndex = Math.max(ChatHeads.renderHeadData.codePointIndex(), 0); // fallback to rendering at beginning

        if (chatheads$charsRendered == renderIndex) {
            if (!dropShadow) {
                // -> chatheads$renderChatHeadBeforeName
                ChatHeads.renderHeadX = (int) x + 1;
                ChatHeads.renderHeadY = (int) y;
                ChatHeads.renderHeadPose = matrix;
            }

            x += 8 + 2;
        }

        chatheads$charsRendered++;
    }
}
