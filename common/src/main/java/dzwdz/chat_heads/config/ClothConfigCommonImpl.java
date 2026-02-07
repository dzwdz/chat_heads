package dzwdz.chat_heads.config;

import dzwdz.chat_heads.ChatHeads;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.world.InteractionResult;

public class ClothConfigCommonImpl {
	private static ConfigHolder<ChatHeadsConfigData> CONFIG_HOLDER;

	public static void loadConfig() {
		var configHolder = AutoConfig.register(ChatHeadsConfigData.class, JanksonConfigSerializer::new);
		ChatHeads.CONFIG = configHolder.getConfig();
		CONFIG_HOLDER = configHolder;

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
				new RenderPositionGuiProvider(),
				field -> field.getName().equals("renderPosition")
		);

		guiRegistry.registerPredicateProvider(
				new AliasesGuiProvider(),
				field -> field.getName().equals("nameAliases")
		);

		guiRegistry.registerPredicateProvider(
				new ThreeDeeNessGuiProvider(),
				field -> field.getName().equals("threeDeeNess")
		);
	}

	public static void saveConfig() {
		CONFIG_HOLDER.save();
	}
}
