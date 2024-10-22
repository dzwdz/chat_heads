package dzwdz.chat_heads.neoforge;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.neoforge.config.ClothConfigImpl;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class ChatHeadsClient {
	public static void init(IEventBus modBus) {
		modBus.addListener(ChatHeadsClient::commonSetup);

		ClothConfigImpl.registerConfigGui();
	}

	private static void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			ClothConfigImpl.loadConfig();

			ChatHeads.disableBeforeName(modId -> ModList.get().isLoaded(modId));
		});
	}
}
