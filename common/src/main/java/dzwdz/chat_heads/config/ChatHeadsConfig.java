package dzwdz.chat_heads.config;

import java.util.Map;

public interface ChatHeadsConfig {
	RenderPosition renderPosition() ;
	boolean offsetNonPlayerText() ;
	SenderDetection senderDetection();
	boolean smartHeuristics();
	boolean handleSystemMessages();
	boolean drawShadow();
	Map<String, String> getNameAliases();
	boolean detectNameAliases();

	void addNameAlias(String nickname, String profileName);
}
