package dzwdz.chat_heads.fabric;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.fabric.config.ClothConfigImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ChatHeadsFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClothConfigImpl.loadConfig();

		ChatHeads.disableBeforeName(modId -> FabricLoader.getInstance().isModLoaded(modId));
	}
}
