package dzwdz.chat_heads.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.config.ThreeDeeNessGuiProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// another hack, close your eyes!
@Mixin(value = AbstractWidget.class, remap = false)
public abstract class AbstractWidgetMixin {
	@Shadow
	public int x;
	@Shadow
	public int y;

	@Shadow
	public abstract int getHeight();

	@Inject(method = "renderButton", at = @At("TAIL"), remap = false, require = 0)
	public void chatheads$renderHeadPreview(PoseStack matrixStack, int i, int j, float f, CallbackInfo ci) {
		if (ThreeDeeNessGuiProvider.abstractWidget != this)
			return;

		var connection = Minecraft.getInstance().getConnection();
		if (connection == null)
			return;

		var uuid = connection.getLocalGameProfile().getId();
		if (uuid == null)
			return;

		var playerInfo = connection.getPlayerInfo(uuid);
		if (playerInfo == null)
			return;

		ChatHeads.renderChatHead(matrixStack, x - 12, y + ((getHeight() - 8) / 2), playerInfo, 1);
	}
}
