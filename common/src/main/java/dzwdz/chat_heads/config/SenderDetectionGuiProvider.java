package dzwdz.chat_heads.config;

import me.shedaniel.autoconfig.gui.registry.api.GuiProvider;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.TranslatableComponent;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class SenderDetectionGuiProvider implements GuiProvider {
	private static final String EXPLANATION = "text.autoconfig.chat_heads.option.explanation";
	private static final String SENDER_DETECTION = "text.autoconfig.chat_heads.option.senderDetection";

	@SuppressWarnings({"rawtypes"})
	@Override
	public List<AbstractConfigListEntry> get(String i13n, Field field, Object config_, Object defaults, GuiRegistryAccess registry) {
		ChatHeadsConfigData config = (ChatHeadsConfigData) config_;

		return Arrays.asList(
				ConfigEntryBuilder.create()
						.startTextDescription(new TranslatableComponent(EXPLANATION))
						.setTooltip(
								new TranslatableComponent(EXPLANATION + ".@Tooltip[0]"),
								new TranslatableComponent(EXPLANATION + ".@Tooltip[1]"),
								new TranslatableComponent(EXPLANATION + ".@Tooltip[2]"),
								new TranslatableComponent(EXPLANATION + ".@Tooltip[3]"),
								new TranslatableComponent(EXPLANATION + ".@Tooltip[4]")
						)
						.build(),
				ConfigEntryBuilder.create()
						.startEnumSelector(new TranslatableComponent(SENDER_DETECTION), SenderDetection.class, config.senderDetection)
						.setDefaultValue(ChatHeadsConfigDefaults.SENDER_DETECTION)
						.setSaveConsumer(senderDetection -> config.senderDetection = senderDetection)
						.setEnumNameProvider(anEnum -> new TranslatableComponent(SENDER_DETECTION + "." + anEnum.name()))
						.build());
	}
}