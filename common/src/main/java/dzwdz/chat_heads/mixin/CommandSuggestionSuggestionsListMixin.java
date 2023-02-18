package dzwdz.chat_heads.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.suggestion.Suggestion;
import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(CommandSuggestions.SuggestionsList.class)
public abstract class CommandSuggestionSuggestionsListMixin {
    @Shadow @Final
    private Rect2i rect;

    @Unique
    PlayerInfo chatHeads$player;

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

    @ModifyArgs(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/lang/String;FFI)I",
                    ordinal = 0
            ),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;II)V"
    )
    public void renderChatHead(Args args) {
        PoseStack poseStack = args.get(0);
        int y = (int) (float) args.get(3);
        int x = rect.getX() - (8 + 2);

        if (chatHeads$player != null) {
            ChatHeads.renderChatHead(poseStack, x, y, chatHeads$player);
            chatHeads$player = null;
        }
    }

}
