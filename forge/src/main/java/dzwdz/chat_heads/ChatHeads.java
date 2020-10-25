package dzwdz.chat_heads;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraftforge.fml.common.Mod;

@Mod("chat_heads")
public class ChatHeads {
    public static NetworkPlayerInfo lastSender;
    public static ChatLine<?> lastGuiMessage;

    public static int lastY = 0;
    public static float lastOpacity = 0;

    public static final int CHAT_OFFSET = 10;

    public ChatHeads() {

    }
}
