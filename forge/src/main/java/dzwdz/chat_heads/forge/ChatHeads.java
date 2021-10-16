package dzwdz.chat_heads.forge;

import dzwdz.chat_heads.config.ChatHeadsConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlclient.ConfigGuiHandler.ConfigGuiFactory;

import static dzwdz.chat_heads.ChatHeads.CONFIG;

@Mod("chat_heads")
public class ChatHeads {
	public ChatHeads() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

		ModLoadingContext.get().registerExtensionPoint(ConfigGuiFactory.class, () -> new ConfigGuiFactory(
				(client, parent) -> AutoConfig.getConfigScreen(ChatHeadsConfig.class, parent).get()));
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			CONFIG = AutoConfig.register(ChatHeadsConfig.class, JanksonConfigSerializer::new).getConfig();
		});
	}
}
