package dzwdz.chat_heads.mixininterface;

import net.minecraft.client.multiplayer.PlayerInfo;

public interface Ownable {
    PlayerInfo chatheads$getOwner();
    void chatheads$setOwner(PlayerInfo playerInfo);
}
