package dzwdz.chat_heads.forge.config;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.config.ChatHeadsConfig;
import dzwdz.chat_heads.config.ChatHeadsConfigData;
import dzwdz.chat_heads.config.MissingClothConfigScreen;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
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
			ChatHeads.CONFIG = new ChatHeadsConfig(AutoConfig.register(ChatHeadsConfigData.class, JanksonConfigSerializer::new).getConfig());
		}
	}
}
