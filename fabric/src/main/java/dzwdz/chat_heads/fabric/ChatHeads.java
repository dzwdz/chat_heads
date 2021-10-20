package dzwdz.chat_heads.fabric;

import dzwdz.chat_heads.fabric.config.ClothConfigImpl;
import net.fabricmc.api.ModInitializer;

public class ChatHeads implements ModInitializer {
	@Override
	public void onInitialize() {
		ClothConfigImpl.loadConfig();
	}
}
