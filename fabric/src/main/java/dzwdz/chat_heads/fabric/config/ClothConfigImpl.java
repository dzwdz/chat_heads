package dzwdz.chat_heads.fabric.config;

import dzwdz.chat_heads.config.ClothConfigCommonImpl;
import net.fabricmc.loader.api.FabricLoader;

public class ClothConfigImpl {
	public static boolean isInstalled() {
		return FabricLoader.getInstance().isModLoaded("cloth-config2");
	}

	public static void loadConfig() {
		if (isInstalled()) {
			ClothConfigCommonImpl.loadConfig();
		}
	}
}
