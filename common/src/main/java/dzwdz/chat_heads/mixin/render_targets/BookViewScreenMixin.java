package dzwdz.chat_heads.mixin.render_targets;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// custom rendering inside books wasn't planned but there is no easy way to *not* do 3Dness there, so this will also enable the offsetting
@Mixin(BookViewScreen.class)
public abstract class BookViewScreenMixin {
    @Inject(method = "render", at = @At("HEAD"))
    public void chatheads$isInsideBook(CallbackInfo ci) {
        ChatHeads.customHeadRendering = true;
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void chatheads$isOutsideBook(CallbackInfo ci) {
        ChatHeads.customHeadRendering = false;
    }
}
