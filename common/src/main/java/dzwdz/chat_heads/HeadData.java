package dzwdz.chat_heads;

import net.minecraft.client.multiplayer.PlayerInfo;

public record HeadData(PlayerInfo playerInfo, int codePointIndex) {
    public static HeadData EMPTY = new HeadData(null, -1);

    public HeadData {
        if (playerInfo == null && codePointIndex != -1)
            throw new AssertionError();
    }

    public static HeadData of(PlayerInfo playerInfo) {
        if (playerInfo == null) return EMPTY;
        return new HeadData(playerInfo, -1);
    }

    public boolean hasHead() {
        return playerInfo != null;
    }

    public boolean hasHeadPosition() {
        return codePointIndex != -1;
    }
}
