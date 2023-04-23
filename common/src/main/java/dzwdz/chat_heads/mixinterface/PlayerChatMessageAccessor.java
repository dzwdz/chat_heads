package dzwdz.chat_heads.mixinterface;

import net.minecraft.client.multiplayer.PlayerInfo;

public interface PlayerChatMessageAccessor {
    PlayerInfo getPlayerInfo();
    void setPlayerInfo(PlayerInfo playerInfo);
}
