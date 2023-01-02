package dzwdz.chat_heads.config;

import dzwdz.chat_heads.ChatHeads;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

public class ClothConfigCommonImpl {
	public static void loadConfig() {
		ChatHeads.CONFIG = AutoConfig.register(ChatHeadsConfigData.class, JanksonConfigSerializer::new).getConfig();

		var guiRegistry = AutoConfig.getGuiRegistry(ChatHeadsConfigData.class);
		guiRegistry.registerPredicateProvider(
				new SenderDetectionGuiProvider(),
				field -> field.getName().equals("senderDetection")
		);

		guiRegistry.registerPredicateProvider(
				new AliasesGuiProvider(),
				field -> field.getName().equals("nameAliases")
		);
	}
}
