package dzwdz.chat_heads.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.suggestion.Suggestion;
import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestions.SuggestionsList.class)
public abstract class CommandSuggestionSuggestionsListMixin {
    @Shadow @Final
    private Rect2i rect;

    @Unique
    PoseStack chatHeads$poseStack;

    @Unique
    PlayerInfo chatHeads$player;

    @ModifyVariable(
            at = @At("HEAD"),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;II)V",
            argsOnly = true
    )
    public PoseStack chatheads$capturePoseStack(PoseStack poseStack) {
        chatHeads$poseStack = poseStack;
        return poseStack;
    }

    @ModifyVariable(
            at = @At("STORE"),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;II)V",
            ordinal = 0
    )
    public Suggestion chatheads$captureSuggestion(Suggestion suggestion) {
        // lookup by profile name - this should be fine on any server
        // since this applies to all suggestions, this might add chat heads in weird places, though very unlikely
        chatHeads$player = Minecraft.getInstance().getConnection().getPlayerInfo(suggestion.getText());
        return suggestion;
    }

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V",
                    ordinal = 4
            ),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;II)V",
            index = 1
    )
    public int chatheads$enlargeBackground(int x) {
        if (chatHeads$player != null) return x - (2 + 8 + 2);
        return x;
    }

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/lang/String;FFI)I",
                    ordinal = 0
            ),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;II)V",
            index = 3
    )
    public float renderChatHead(float y) {
        int x = rect.getX() - (8 + 2);

        if (chatHeads$player != null) {
            ChatHeads.renderChatHead(chatHeads$poseStack, x, (int) y, chatHeads$player);
            chatHeads$player = null;
        }

        return y;
    }

    @Inject(
            at = @At("RETURN"),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;II)V"
    )
    public void chatheads$forgetPoseStack(PoseStack poseStack, int i, int j, CallbackInfo ci) {
        chatHeads$poseStack = null;
    }
}
