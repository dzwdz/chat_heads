package dzwdz.chat_heads.config;

import me.shedaniel.autoconfig.gui.registry.api.GuiProvider;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.List;

public class RenderPositionGuiProvider implements GuiProvider {
	private static final String RENDER_POSITION = "text.autoconfig.chat_heads.option.renderPosition";

	@SuppressWarnings({"rawtypes"})
	@Override
	public List<AbstractConfigListEntry> get(String i13n, Field field, Object config_, Object defaults, GuiRegistryAccess registry) {
		ChatHeadsConfigData config = (ChatHeadsConfigData) config_;

		return List.of(
				ConfigEntryBuilder.create()
						.startEnumSelector(Component.translatable(RENDER_POSITION), RenderPosition.class, config.renderPosition)
						.setDefaultValue(ChatHeadsConfigDefaults.RENDER_POSITION)
						.setSaveConsumer(renderPosition -> config.renderPosition = renderPosition)
						.setEnumNameProvider(anEnum -> Component.translatable(RENDER_POSITION + "." + anEnum.name()))
						.build());
	}
}