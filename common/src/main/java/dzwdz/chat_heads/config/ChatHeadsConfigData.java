package dzwdz.chat_heads.config;

import dzwdz.chat_heads.ChatHeads;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

// can't be instantiated when Cloth Config isn't installed
@SuppressWarnings("CanBeFinal")
@Config(name = ChatHeads.MOD_ID)
public class ChatHeadsConfigData implements ConfigData {
	@ConfigEntry.Gui.Tooltip()
	public boolean offsetNonPlayerText = true;
	@ConfigEntry.Gui.Tooltip(count = 5)
	public boolean smartHeuristics = true;
}