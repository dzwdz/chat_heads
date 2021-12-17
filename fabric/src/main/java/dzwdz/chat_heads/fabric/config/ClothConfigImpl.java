package dzwdz.chat_heads.fabric.config;

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.config.ChatHeadsConfig;
import dzwdz.chat_heads.config.ChatHeadsConfigData;
import dzwdz.chat_heads.config.SenderDetectionGuiProvider;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.loader.api.FabricLoader;

public class ClothConfigImpl {
	public static boolean isInstalled() {
		return FabricLoader.getInstance().isModLoaded("cloth-config2");
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
