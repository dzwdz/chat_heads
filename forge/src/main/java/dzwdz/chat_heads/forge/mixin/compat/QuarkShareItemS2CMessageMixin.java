package dzwdz.chat_heads.forge.mixin.compat;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.HeadData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Pseudo
@Mixin(targets = "org.violetmoon.quark.base.network.message.ShareItemS2CMessage")
public abstract class QuarkShareItemS2CMessageMixin {
	@Shadow(remap = false) UUID senderUuid;

	@Inject(
		method = "lambda$receive$0()V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"
		)
	)
	private void chatheads$setOwner(CallbackInfo ci) {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			ChatHeads.lastSenderData = HeadData.of(connection.getPlayerInfo(senderUuid));
		}
	}
}
