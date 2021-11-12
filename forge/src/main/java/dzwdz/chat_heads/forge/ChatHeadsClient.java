package dzwdz.chat_heads.forge;

import dzwdz.chat_heads.forge.config.ClothConfigImpl;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("chat_heads")
public class ChatHeadsClient {
	public static void init() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ChatHeadsClient::commonSetup);

		ClothConfigImpl.registerConfigGui();
	}

	private static void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(ClothConfigImpl::loadConfig);
	}
}
