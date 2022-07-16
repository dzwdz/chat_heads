package dzwdz.chat_heads.forge.config;

import dzwdz.chat_heads.config.ChatHeadsConfigData;
import dzwdz.chat_heads.config.ClothConfigCommonImpl;
import dzwdz.chat_heads.config.MissingClothConfigScreen;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

public class ClothConfigImpl {
	private static boolean isInstalled() {
		return ModList.get().isLoaded("cloth_config");
	}

	public static void registerConfigGui() {
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, () -> new ConfigScreenFactory(
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
