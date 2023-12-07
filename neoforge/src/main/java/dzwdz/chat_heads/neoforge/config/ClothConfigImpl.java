package dzwdz.chat_heads.neoforge.config;

import dzwdz.chat_heads.config.ChatHeadsConfigData;
import dzwdz.chat_heads.config.ClothConfigCommonImpl;
import dzwdz.chat_heads.config.MissingClothConfigScreen;
import me.shedaniel.autoconfig.AutoConfig;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.ConfigScreenHandler;


public class ClothConfigImpl {
	private static boolean isInstalled() {
		return ModList.get().isLoaded("cloth_config");
	}

	public static void registerConfigGui() {
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(
				(client, parent) -> {
					if (isInstalled()) {
						return AutoConfig.getConfigScreen(ChatHeadsConfigData.class, parent).get();
					} else {
						return new MissingClothConfigScreen(parent);
					}
				}));
	}

	public static void loadConfig() {
		if (isInstalled()) {
			ClothConfigCommonImpl.loadConfig();
		}
	}
}
