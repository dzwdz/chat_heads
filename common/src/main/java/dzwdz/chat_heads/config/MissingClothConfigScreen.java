package dzwdz.chat_heads.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

public class MissingClothConfigScreen extends Screen {
	private final Screen parent;
	private final Component message;
	private final Component clothConfigCfLink;
	private final Component clothConfigMrLink;

	public MissingClothConfigScreen(Screen parent) {
		super(Component.translatable("text.chat_heads.config.error.title"));
		this.message = Component.translatable("text.chat_heads.config.error.no_cloth_config");
		this.clothConfigCfLink = Component.translatable("text.chat_heads.config.error.cloth_config_curseforge_link");
		this.clothConfigMrLink = Component.translatable("text.chat_heads.config.error.cloth_config_modrinth_link");
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		// button dimensions
		int w = 220;
		int h = 20;
		int x = width / 2 - w / 2; // centered
		int y = 120;

		addRenderableWidget(Button.builder(clothConfigCfLink, (button) -> {
			Util.getPlatform().openUri("https://www.curseforge.com/minecraft/mc-mods/cloth-config");
		}).bounds(x, y, w, h).build());

		y += h + 10;

		addRenderableWidget(Button.builder(clothConfigMrLink, (button) -> {
			Util.getPlatform().openUri("https://modrinth.com/mod/cloth-config");
		}).bounds(x, y, w, h).build());

		y += h + 10;

		addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> {
			minecraft.setScreen(parent);
		}).bounds(x, y, w, h).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		super.render(guiGraphics, mouseX, mouseY, delta);
		guiGraphics.drawCenteredString(font, title, width / 2, 80, -1);
		guiGraphics.drawCenteredString(font, message, width / 2, 100, -1);
	}
}