package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.mixinterface.PlayerChatMessageAccessor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/** Pass the sender's PlayerInfo through the PlayerChatMessage */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
	@Shadow
	@Nullable
	public abstract PlayerInfo getPlayerInfo(UUID uUID);

	@Unique
	private PlayerInfo chatheads$senderInfo;

	@Inject(
			method = "handlePlayerChat(Lnet/minecraft/network/protocol/game/ClientboundPlayerChatPacket;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/chat/ChatListener;handlePlayerChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/network/chat/ChatType$Bound;)V"
			)
	)
	public void chatheads$captureSenderInfo(ClientboundPlayerChatPacket packet, CallbackInfo ci) {
		// could use a locals capture but this feels more compatible (and it's just a map lookup)
		chatheads$senderInfo = getPlayerInfo(packet.sender());
	}

	@ModifyArg(
			method = "handlePlayerChat(Lnet/minecraft/network/protocol/game/ClientboundPlayerChatPacket;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/chat/ChatListener;handlePlayerChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/network/chat/ChatType$Bound;)V"
			),
			index = 0
	)
	public PlayerChatMessage chatheads$rememberSenderInfo(PlayerChatMessage playerChatMessage) {
		((PlayerChatMessageAccessor) (Object) playerChatMessage).setPlayerInfo(chatheads$senderInfo);
		return playerChatMessage;
	}
}
