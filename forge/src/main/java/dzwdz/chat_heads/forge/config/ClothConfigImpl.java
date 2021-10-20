package dzwdz.chat_heads.forge.config;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.config.ChatHeadsConfig;
import dzwdz.chat_heads.config.ChatHeadsConfigData;
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
		if (isInstalled()) {
			ModLoadingContext.get().registerExtensionPoint(ConfigGuiFactory.class, () -> new ConfigGuiFactory(
					(client, parent) -> AutoConfig.getConfigScreen(ChatHeadsConfigData.class, parent).get()));
		}
	}

	public static void loadConfig() {
		if (isInstalled()) {
			ChatHeads.CONFIG = new ChatHeadsConfig(AutoConfig.register(ChatHeadsConfigData.class, JanksonConfigSerializer::new).getConfig());
		}
	}
}
