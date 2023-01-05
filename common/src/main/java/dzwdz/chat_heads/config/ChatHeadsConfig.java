package dzwdz.chat_heads.config;

public interface ChatHeadsConfig {
	boolean offsetNonPlayerText() ;
	SenderDetection senderDetection();
	boolean smartHeuristics();

	/** Attempts to resolve nicknames into profile names, returning the nickname if there's no alias. */
	String getProfileName(String nickname);
}
