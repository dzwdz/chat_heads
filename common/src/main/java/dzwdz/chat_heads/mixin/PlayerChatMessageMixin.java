package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.mixinterface.PlayerChatMessageAccessor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerChatMessage.class)
public abstract class PlayerChatMessageMixin implements PlayerChatMessageAccessor {
	private PlayerInfo chatheads$playerInfo;

	@Override
	public void setPlayerInfo(PlayerInfo playerInfo) {
		chatheads$playerInfo = playerInfo;
	}

	@Override
	public PlayerInfo getPlayerInfo() {
		return chatheads$playerInfo;
	}
}
