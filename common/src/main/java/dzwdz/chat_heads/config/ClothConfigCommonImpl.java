package dzwdz.chat_heads.config;

import dzwdz.chat_heads.ChatHeads;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.world.InteractionResult;

public class ClothConfigCommonImpl {
	public static void loadConfig() {
		var configHolder = AutoConfig.register(ChatHeadsConfigData.class, JanksonConfigSerializer::new);
		ChatHeads.CONFIG = configHolder.getConfig();

		configHolder.registerSaveListener((manager, data) -> {
			try {
				data.validatePostLoad();
			} catch (ConfigData.ValidationException ignored) { }
			return InteractionResult.SUCCESS;
		});

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
