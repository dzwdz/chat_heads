package dzwdz.chat_heads.forge;

import dzwdz.chat_heads.forge.config.ClothConfigImpl;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("chat_heads")
public class ChatHeads {
	public ChatHeads() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

		ClothConfigImpl.registerConfigGui();
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(ClothConfigImpl::loadConfig);
	}
}
