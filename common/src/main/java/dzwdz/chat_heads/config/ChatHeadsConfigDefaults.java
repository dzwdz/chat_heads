package dzwdz.chat_heads.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChatHeadsConfigDefaults implements ChatHeadsConfig {
	public static final RenderPosition RENDER_POSITION = RenderPosition.BEFORE_NAME;
	public static final boolean OFFSET_NON_PLAYER_TEXT = true;
	public static final SenderDetection SENDER_DETECTION = SenderDetection.UUID_AND_HEURISTIC;
	public static final boolean SMART_HEURISTICS = true;
	public static final boolean HANDLE_SYSTEM_MESSAGES = true;
	public static final boolean DRAW_SHADOW = true;

	public static final boolean DETECT_ALIASES = true;
	public Map<String, String> nameAliases = new LinkedHashMap<>(); // nickname -> profile name

	@Override
	public RenderPosition renderPosition() {
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
		return nameAliases;
	}

	@Override
	public boolean detectNameAliases() {
		return DETECT_ALIASES;
	}

	@Override
	public void addNameAlias(String nickname, String profileName) {
		nameAliases.put(nickname, profileName);
	}

	@Override
	public boolean drawShadow() {
		return DRAW_SHADOW;
	}
}
