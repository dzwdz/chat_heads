package dzwdz.chat_heads.config;

import dzwdz.chat_heads.ChatHeads;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = ChatHeads.MOD_ID)
public class ChatHeadsConfig implements ConfigData {
	@ConfigEntry.Gui.Tooltip()
	public boolean offsetNonPlayerText = true;
}