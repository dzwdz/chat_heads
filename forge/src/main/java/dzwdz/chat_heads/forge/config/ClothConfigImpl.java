package dzwdz.chat_heads.forge.config;

import dzwdz.chat_heads.config.ChatHeadsConfigData;
import dzwdz.chat_heads.config.ClothConfigCommonImpl;
import dzwdz.chat_heads.config.MissingClothConfigScreen;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

public class ClothConfigImpl {
	private static boolean isInstalled() {
		return ModList.get().getModFileById("cloth-config") != null; // Note: changed from '-' to '_' in v5
	}

	public static void registerConfigGui() {
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () ->
				(client, parent) -> {
					if (isInstalled()) {
						return AutoConfig.getConfigScreen(ChatHeadsConfigData.class, parent).get();
					} else {
						return new MissingClothConfigScreen(parent, true);
					}
				});
	}

	public static void loadConfig() {
		if (isInstalled()) {
			ClothConfigCommonImpl.loadConfig();
		}
	}
}
