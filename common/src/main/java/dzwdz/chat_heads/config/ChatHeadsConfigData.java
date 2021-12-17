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
	public boolean offsetNonPlayerText = Defaults.OFFSET_NON_PLAYER_TEXT;
	@ConfigEntry.Gui.Tooltip(count = 3)
	public SenderDetection senderDetection = Defaults.SENDER_DETECTION;
	@ConfigEntry.Gui.Tooltip
	public boolean smartHeuristics = Defaults.SMART_HEURISTICS;
}