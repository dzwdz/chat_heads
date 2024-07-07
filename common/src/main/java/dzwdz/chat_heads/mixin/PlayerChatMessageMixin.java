package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.mixininterface.Ownable;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerChatMessage.class)
public abstract class PlayerChatMessageMixin implements Ownable {
	@Unique
	private PlayerInfo chatheads$owner;

	@Override
	public void chatheads$setOwner(PlayerInfo playerInfo) {
		chatheads$owner = playerInfo;
	}

	@Override
	public PlayerInfo chatheads$getOwner() {
		return chatheads$owner;
	}
}
