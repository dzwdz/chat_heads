package dzwdz.chat_heads.forge.config;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.config.ChatHeadsConfig;
import dzwdz.chat_heads.config.ChatHeadsConfigData;
import dzwdz.chat_heads.config.MissingClothConfigScreen;
import dzwdz.chat_heads.config.SenderDetectionGuiProvider;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

public class ClothConfigImpl {
	private static boolean isInstalled() {
		return ModList.get().isLoaded("cloth_config");
	}

	public static void registerConfigGui() {
		ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory(
				(client, parent) -> {
					if (isInstalled()) {
						return AutoConfig.getConfigScreen(ChatHeadsConfigData.class, parent).get();
					} else {
						return new MissingClothConfigScreen(parent, true);
					}
				}));
	}

	public static void loadConfig() {
		if (isInstalled()) {
			ChatHeads.CONFIG = new ChatHeadsConfig(AutoConfig.register(ChatHeadsConfigData.class, JanksonConfigSerializer::new).getConfig());

			AutoConfig.getGuiRegistry(ChatHeadsConfigData.class).registerPredicateProvider(
					new SenderDetectionGuiProvider(),
					field -> field.getName().equals("senderDetection")
			);
		}
	}
}
