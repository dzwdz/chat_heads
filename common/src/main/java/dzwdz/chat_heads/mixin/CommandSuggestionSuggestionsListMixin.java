package dzwdz.chat_heads.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
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

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    public GuiGraphics chatheads$captureGuiGraphics(GuiGraphics guiGraphics, @Share("graphics") LocalRef<GuiGraphics> graphicsRef) {
        graphicsRef.set(guiGraphics);
        return guiGraphics;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void chatheads$fixOutOfBoundChatHeads(CommandSuggestions commandSuggestions, int x, int y, int width, List<Suggestion> suggestions, boolean narrateFirstSuggestion, CallbackInfo ci) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;

        // when chat head would render out of bounds
        if (rect.getX() - (2 + 8 + 2) < 3) {
            // and when a chat head could render at all
            for (Suggestion suggestion : suggestionList) {
                PlayerInfo playerInfo = connection.getPlayerInfo(suggestion.getText());
                if (playerInfo != null) {
                    // move suggestions to accommodate for chat heads
                    rect.setPosition(3 + (2 + 8 + 2), rect.getY());
                    break;
                }
            }
        }
    }

    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 0)
    public Suggestion chatheads$captureSuggestion(Suggestion suggestion,
            @Share("player") LocalRef<PlayerInfo> playerRef) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return suggestion;

        // lookup by profile name - this should be fine on any server
        // since this applies to all suggestions, this might add chat heads in weird places, though very unlikely
        playerRef.set(connection.getPlayerInfo(suggestion.getText()));

        return suggestion;
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V",
                    ordinal = 4
            ),
            index = 0
    )
    public int chatheads$enlargeBackground(int x,
            @Share("player") LocalRef<PlayerInfo> playerRef) {
        if (playerRef.get() != null) return x - (2 + 8 + 2);
        return x;
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I",
                    ordinal = 0
            ),
            index = 3
    )
    public int chatheads$renderChatHead(int y,
            @Share("player") LocalRef<PlayerInfo> playerRef, @Share("graphics") LocalRef<GuiGraphics> graphicsRef) {
        int x = rect.getX() - (8 + 2);

        if (playerRef.get() != null) {
            ChatHeads.renderChatHead(graphicsRef.get(), x, y, playerRef.get(), 1.0f);
        }

        return y;
    }
}
