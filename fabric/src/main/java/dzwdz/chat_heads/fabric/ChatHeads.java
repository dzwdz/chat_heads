package dzwdz.chat_heads.fabric;

import dzwdz.chat_heads.fabric.config.ClothConfigImpl;
import net.fabricmc.api.ClientModInitializer;

public class ChatHeads implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClothConfigImpl.loadConfig();
	}
}
