package dzwdz.chat_heads.config;

import dzwdz.chat_heads.ChatHeads;

import java.util.Map;

public class ChatHeadsConfigDefaults implements ChatHeadsConfig {
	public static final RenderPosition RENDER_POSITION = RenderPosition.BEFORE_NAME;
	public static final boolean OFFSET_NON_PLAYER_TEXT = true;
	public static final SenderDetection SENDER_DETECTION = SenderDetection.UUID_AND_HEURISTIC;
	public static final boolean SMART_HEURISTICS = true;
	public static final boolean HANDLE_SYSTEM_MESSAGES = true;
	public static final boolean DRAW_SHADOW = true;

	@Override
	public RenderPosition renderPosition() {
		if (ChatHeads.forceBeforeLine)
			return RenderPosition.BEFORE_LINE;

		return RENDER_POSITION;
	}

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
	public Map<String, String> getNameAliases() {
		return Map.of();
	}

	@Override
	public boolean drawShadow() {
		return DRAW_SHADOW;
	}
}
