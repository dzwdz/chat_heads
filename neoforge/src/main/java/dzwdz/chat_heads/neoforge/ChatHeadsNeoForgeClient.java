package dzwdz.chat_heads.neoforge;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.neoforge.config.ClothConfigImpl;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class ChatHeadsNeoForgeClient {
	public static void init(IEventBus modBus) {
		modBus.addListener(ChatHeadsNeoForgeClient::commonSetup);

		ClothConfigImpl.registerConfigGui();
	}

	private static void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(ChatHeads::init);
	}
}
