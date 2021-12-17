package dzwdz.chat_heads.config;

import dzwdz.chat_heads.ChatHeads;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

// can't be instantiated when Cloth Config isn't installed
@SuppressWarnings("CanBeFinal")
@Config(name = ChatHeads.MOD_ID)
public class ChatHeadsConfigData implements ConfigData, ChatHeadsConfig {
	@ConfigEntry.Gui.Tooltip()
	public boolean offsetNonPlayerText = ChatHeadsConfigDefaults.OFFSET_NON_PLAYER_TEXT;
	@ConfigEntry.Gui.Tooltip(count = 3)
	public SenderDetection senderDetection = ChatHeadsConfigDefaults.SENDER_DETECTION;
	@ConfigEntry.Gui.Tooltip(count = 2)
	public boolean smartHeuristics = ChatHeadsConfigDefaults.SMART_HEURISTICS;

	@Override
	public boolean offsetNonPlayerText() {
		return offsetNonPlayerText;
	}

	@Override
	public SenderDetection senderDetection() {
		return senderDetection;
	}

	@Override
	public boolean smartHeuristics() {
		return smartHeuristics;
	}
}