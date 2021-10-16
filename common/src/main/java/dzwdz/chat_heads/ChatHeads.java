package dzwdz.chat_heads;

import dzwdz.chat_heads.config.ChatHeadsConfig;
import dzwdz.chat_heads.mixinterface.GuiMessageOwnerAccessor;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.jetbrains.annotations.Nullable;

public class ChatHeads {
    public static final String MOD_ID = "chat_heads";
    public static ChatHeadsConfig CONFIG;

    @Nullable
    public static PlayerInfo lastSender;
    @Nullable
    public static GuiMessage<?> lastGuiMessage;

    public static int lastY = 0;
    public static float lastOpacity = 0.0f;

    public static final int CHAT_OFFSET = 10;
}
