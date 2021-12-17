package dzwdz.chat_heads.config;

public class ChatHeadsConfig {
	private final ChatHeadsConfigData configData;

	public ChatHeadsConfig() {
		this.configData = null;
	}

	public ChatHeadsConfig(ChatHeadsConfigData configData) {
		this.configData = configData;
	}

	public boolean offsetNonPlayerText() {
		return configData == null ? Defaults.OFFSET_NON_PLAYER_TEXT : configData.offsetNonPlayerText;
	}

	public SenderDetection senderDetection() {
		return configData == null ? Defaults.SENDER_DETECTION : configData.senderDetection;
	}

	public boolean smartHeuristics() {
		return configData == null ? Defaults.SMART_HEURISTICS : configData.smartHeuristics;
	}
}
