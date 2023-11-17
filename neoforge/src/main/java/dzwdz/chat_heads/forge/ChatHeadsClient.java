package dzwdz.chat_heads.forge;

import dzwdz.chat_heads.forge.config.ClothConfigImpl;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

public class ChatHeadsClient {
	public static void init() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ChatHeadsClient::commonSetup);

		ClothConfigImpl.registerConfigGui();
	}

	private static void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(ClothConfigImpl::loadConfig);
	}
}
