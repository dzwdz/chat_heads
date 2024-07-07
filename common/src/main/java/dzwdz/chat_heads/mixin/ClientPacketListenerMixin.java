package dzwdz.chat_heads.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/** Pass the sender's PlayerInfo through the PlayerChatMessage */
@Mixin(value = ClientPacketListener.class, priority = 990) // apply before EssentialClient (else the callbacks will land inside the @WrapConditional introduced ifs)
public abstract class ClientPacketListenerMixin {
	@Shadow
	@Nullable
	public abstract PlayerInfo getPlayerInfo(UUID uuid);

	@Inject(
			method = "handlePlayerChat(Lnet/minecraft/network/protocol/game/ClientboundPlayerChatPacket;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/chat/ChatListener;handlePlayerChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/network/chat/ChatType$Bound;)V"
			)
	)
	public void chatheads$captureSenderInfo(ClientboundPlayerChatPacket packet, CallbackInfo ci,
			@Share("senderInfo") LocalRef<PlayerInfo> senderInfo) {
		var playerInfo = getPlayerInfo(packet.sender());
		senderInfo.set(playerInfo);
	}

	@ModifyArg(
			method = "handlePlayerChat(Lnet/minecraft/network/protocol/game/ClientboundPlayerChatPacket;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/chat/ChatListener;handlePlayerChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/network/chat/ChatType$Bound;)V"
			),
			index = 0
	)
	public PlayerChatMessage chatheads$rememberSenderInfo(PlayerChatMessage playerChatMessage,
			@Share("senderInfo") LocalRef<PlayerInfo> senderInfo) {
		ChatHeads.setOwner(playerChatMessage, senderInfo.get());
		return playerChatMessage;
	}
}
