package dzwdz.chat_heads.config;

@SuppressWarnings("SimplifiableConditionalExpression")
public class ChatHeadsConfig {
	private final ChatHeadsConfigData configData;

	public ChatHeadsConfig() {
		this.configData = null;
	}

	public ChatHeadsConfig(ChatHeadsConfigData configData) {
		this.configData = configData;
	}

	public boolean offsetNonPlayerText() {
		return configData == null ? true : configData.offsetNonPlayerText;
	}

	public boolean smartHeuristics() {
		return configData == null ? true : configData.smartHeuristics;
	}
}
