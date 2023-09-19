package dzwdz.chat_heads.config;

public class ChatHeadsConfigDefaults implements ChatHeadsConfig {
	public static final boolean OFFSET_NON_PLAYER_TEXT = true;
	public static final SenderDetection SENDER_DETECTION = SenderDetection.UUID_AND_HEURISTIC;
	public static final boolean SMART_HEURISTICS = true;
	public static final boolean HANDLE_SYSTEM_MESSAGES = false;

	@Override
	public boolean offsetNonPlayerText() {
		return OFFSET_NON_PLAYER_TEXT;
	}

	@Override
	public SenderDetection senderDetection() {
		return SENDER_DETECTION;
	}

	@Override
	public boolean smartHeuristics() {
		return SMART_HEURISTICS;
	}

	@Override
	public boolean handleSystemMessages() {
		return HANDLE_SYSTEM_MESSAGES;
	}

	@Override
	public String getProfileName(String nickname) {
		return nickname;
	}
}
