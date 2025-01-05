package dzwdz.chat_heads.forge;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.forge.config.ClothConfigImpl;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ChatHeadsForgeClient {
	public static void init(FMLJavaModLoadingContext context) {
		context.getModEventBus().addListener(ChatHeadsForgeClient::commonSetup);

		ClothConfigImpl.registerConfigGui(context);
	}

	private static void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(ChatHeads::init);
	}
}
