package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.config.ThreeDeeNessGuiProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// another hack, close your eyes!
@Mixin(value = AbstractSliderButton.class, remap = false)
public abstract class AbstractSliderButtonMixin extends AbstractWidget {
	public AbstractSliderButtonMixin(int i, int j, int k, int l, Component component) {
		super(i, j, k, l, component);
	}

	@Inject(method = "renderWidget", at = @At("TAIL"), remap = false, require = 0)
	public void chatheads$renderHeadPreview(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
		if (ThreeDeeNessGuiProvider.abstractSliderButton != this)
			return;

		var playerInfo = new PlayerInfo(Minecraft.getInstance().getGameProfile(), false);
		ChatHeads.renderChatHead(guiGraphics, getX() - 12, getY() + ((getHeight() - 8) / 2), playerInfo, 1);
	}
}
