package dzwdz.chat_heads.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class MissingClothConfigScreen extends Screen {
	private static final int WHITE = 16777215;

	private final Screen parent;
	private final Component message;
	private final Component clothConfigLink;
	private final boolean forgeLink;

	public MissingClothConfigScreen(Screen parent, boolean forgeLink) {
		super(new TranslatableComponent("text.chat_heads.config.error.title"));
		this.message = new TranslatableComponent("text.chat_heads.config.error.no_cloth_config");
		this.clothConfigLink = new TranslatableComponent("text.chat_heads.config.error.cloth_config_link");
		this.parent = parent;
		this.forgeLink = forgeLink;
	}

	@Override
	protected void init() {
		super.init();
		// button dimensions
		int w = 220;
		int h = 20;
		int x = width / 2 - w / 2; // centered
		int y = 120;

		addButton(new Button(x, y, w, h, clothConfigLink, (button) -> {
			Util.getPlatform().openUri(forgeLink
					? "https://www.curseforge.com/minecraft/mc-mods/cloth-config-forge"
					: "https://www.curseforge.com/minecraft/mc-mods/cloth-config");
		}));

		y += h + 10;

		addButton(new Button(x, y, w, h, CommonComponents.GUI_BACK, (button) -> {
			minecraft.setScreen(parent);
		}));
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
		renderDirtBackground(0);
		drawCenteredString(poseStack, font, title, width / 2, 80, WHITE);
		drawCenteredString(poseStack, font, message, width / 2, 100, WHITE);
		super.render(poseStack, mouseX, mouseY, delta);
	}
}