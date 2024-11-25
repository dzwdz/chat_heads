package dzwdz.chat_heads.fabric;

import dzwdz.chat_heads.ChatHeads;
import net.fabricmc.api.ClientModInitializer;

public class ChatHeadsFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ChatHeads.init();
	}
}
