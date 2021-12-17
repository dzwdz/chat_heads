package dzwdz.chat_heads.config;

public class ChatHeadsConfigDefaults implements ChatHeadsConfig {
	public static final boolean OFFSET_NON_PLAYER_TEXT = true;
	public static final SenderDetection SENDER_DETECTION = SenderDetection.UUID_AND_HEURISTIC;
	public static final boolean SMART_HEURISTICS = true;

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
}
