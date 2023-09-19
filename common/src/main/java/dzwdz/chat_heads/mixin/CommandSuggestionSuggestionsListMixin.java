package dzwdz.chat_heads.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

import java.util.List;

@Mixin(CommandSuggestions.SuggestionsList.class)
public abstract class CommandSuggestionSuggestionsListMixin {
    @Shadow @Final
    private Rect2i rect;
    @Shadow @Final
    private List<Suggestion> suggestionList;

    @Unique
    GuiGraphics chatheads$guiGraphics;

    @Unique
    PlayerInfo chatheads$player;

    @ModifyVariable(
            at = @At("HEAD"),
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            argsOnly = true
    )
    public GuiGraphics chatheads$capturePoseStack(GuiGraphics guiGraphics) {
        chatheads$guiGraphics = guiGraphics;
        return guiGraphics;
    }

    @Inject(at = @At("RETURN"), method = "<init>")
    public void chatheads$fixOutOfBoundChatHeads(CommandSuggestions commandSuggestions, int x, int y, int width, List<Suggestion> suggestions, boolean bl, CallbackInfo ci) {
        // when chat head would render out of bounds
        if (rect.getX() - (2 + 8 + 2) < 3) {
            // and when a chat head could render at all
            for (int i = 0; i < suggestionList.size(); i++) {
                PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(suggestionList.get(i).getText());
                if (playerInfo != null) {
                    // move suggestions to accommodate for chat heads
                    rect.setPosition(3 + (2 + 8 + 2), rect.getY());
                    break;
                }
            }
        }
    }

    @ModifyVariable(
            at = @At("STORE"),
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            ordinal = 0
    )
    public Suggestion chatheads$captureSuggestion(Suggestion suggestion) {
        // lookup by profile name - this should be fine on any server
        // since this applies to all suggestions, this might add chat heads in weird places, though very unlikely
        chatheads$player = Minecraft.getInstance().getConnection().getPlayerInfo(suggestion.getText());
        return suggestion;
    }

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V",
                    ordinal = 4
            ),
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            index = 0
    )
    public int chatheads$enlargeBackground(int x) {
        if (chatheads$player != null) return x - (2 + 8 + 2);
        return x;
    }

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I",
                    ordinal = 0
            ),
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            index = 3
    )
    public int chatheads$renderChatHead(int y) {
        int x = rect.getX() - (8 + 2);

        if (chatheads$player != null) {
            ChatHeads.renderChatHead(chatheads$guiGraphics, x, y, chatheads$player);
            chatheads$player = null;
        }

        return y;
    }

    @Inject(
            at = @At("RETURN"),
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;II)V"
    )
    public void chatheads$forgetPoseStack(GuiGraphics guiGraphics, int i, int j, CallbackInfo ci) {
        chatheads$guiGraphics = null;
    }
}
