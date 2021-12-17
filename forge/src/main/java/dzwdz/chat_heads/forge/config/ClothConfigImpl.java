package dzwdz.chat_heads.forge.config;

import dzwdz.chat_heads.config.ChatHeadsConfigData;
import dzwdz.chat_heads.config.ClothConfigCommonImpl;
import dzwdz.chat_heads.config.MissingClothConfigScreen;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fmlclient.ConfigGuiHandler.ConfigGuiFactory;

public class ClothConfigImpl {
	private static boolean isInstalled() {
		return ModList.get().getModFileById("cloth_config") != null;
	}

	public static void registerConfigGui() {
		ModLoadingContext.get().registerExtensionPoint(ConfigGuiFactory.class, () -> new ConfigGuiFactory(
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
			ClothConfigCommonImpl.loadConfig();
		}
	}
}
