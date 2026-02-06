package dzwdz.chat_heads.config;

import dzwdz.chat_heads.ChatHeads;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.LinkedHashMap;
import java.util.Map;

// can't be instantiated when Cloth Config isn't installed
@SuppressWarnings("CanBeFinal")
@Config(name = ChatHeads.MOD_ID)
public class ChatHeadsConfigData implements ConfigData, ChatHeadsConfig {
	public RenderPosition renderPosition = ChatHeadsConfigDefaults.RENDER_POSITION;
	public float threeDeeNess = ChatHeadsConfigDefaults.THREE_DEE_NESS;
	@ConfigEntry.Gui.Tooltip()
	public boolean offsetNonPlayerText = ChatHeadsConfigDefaults.OFFSET_NON_PLAYER_TEXT;
	@ConfigEntry.Gui.Tooltip()
	public SenderDetection senderDetection = ChatHeadsConfigDefaults.SENDER_DETECTION;
	@ConfigEntry.Gui.Tooltip()
	public boolean smartHeuristics = ChatHeadsConfigDefaults.SMART_HEURISTICS;
	@ConfigEntry.Gui.Tooltip()
	public boolean handleSystemMessages = ChatHeadsConfigDefaults.HANDLE_SYSTEM_MESSAGES;
	public boolean drawShadow = ChatHeadsConfigDefaults.DRAW_SHADOW;

	@ConfigEntry.Gui.Tooltip()
	public Map<String, String> nameAliases = new LinkedHashMap<>(); // nickname -> profile name

	@ConfigEntry.Gui.Tooltip()
	public static boolean detectNameAliases = ChatHeadsConfigDefaults.DETECT_ALIASES;

	@Override
	public RenderPosition renderPosition() {
		return renderPosition;
	}

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

	@Override
	public boolean handleSystemMessages() {
		return handleSystemMessages;
	}

	@Override
	public boolean drawShadow() {
		return drawShadow;
	}

	@Override
	public Map<String, String> getNameAliases() {
		return nameAliases;
	}

	@Override
	public boolean detectNameAliases() {
		return detectNameAliases;
	}

	@Override
	public float threeDeeNess() {
		return threeDeeNess;
	}

	@Override
	public void setThreeDeeNess(float value) {
		threeDeeNess = value;
	}

	@Override
	public void addNameAlias(String nickname, String profileName) {
		nameAliases.put(nickname, profileName);

		ClothConfigCommonImpl.saveConfig();
	}

	@Override
	public void validatePostLoad() throws ConfigData.ValidationException {
		nameAliases.entrySet().removeIf(entry -> entry.getKey().isEmpty() || entry.getValue().isEmpty());
	}
}