package dzwdz.chat_heads.config;

import me.shedaniel.autoconfig.gui.registry.api.GuiProvider;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.List;

public class SenderDetectionGuiProvider implements GuiProvider {
	private static final String EXPLANATION = "text.autoconfig.chat_heads.option.explanation";
	private static final String SENDER_DETECTION = "text.autoconfig.chat_heads.option.senderDetection";

	@SuppressWarnings({"rawtypes"})
	@Override
	public List<AbstractConfigListEntry> get(String i13n, Field field, Object config_, Object defaults, GuiRegistryAccess registry) {
		ChatHeadsConfigData config = (ChatHeadsConfigData) config_;

		return List.of(
				ConfigEntryBuilder.create()
						.startTextDescription(Component.translatable(EXPLANATION))
						.setTooltip(Component.translatable(EXPLANATION + ".@Tooltip"))
						.build(),
				ConfigEntryBuilder.create()
						.startEnumSelector(Component.translatable(SENDER_DETECTION), SenderDetection.class, config.senderDetection)
						.setDefaultValue(ChatHeadsConfigDefaults.SENDER_DETECTION)
						.setSaveConsumer(senderDetection -> config.senderDetection = senderDetection)
						.setEnumNameProvider(anEnum -> Component.translatable(SENDER_DETECTION + "." + anEnum.name()))
						.build());
	}
}