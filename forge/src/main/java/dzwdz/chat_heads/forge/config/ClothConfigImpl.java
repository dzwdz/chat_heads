package dzwdz.chat_heads.forge.config;

import dzwdz.chat_heads.Compat;
import dzwdz.chat_heads.config.ChatHeadsConfigData;
import dzwdz.chat_heads.config.MissingClothConfigScreen;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClothConfigImpl {
	public static void registerConfigGui(FMLJavaModLoadingContext context) {
		context.registerExtensionPoint(ConfigScreenFactory.class, () -> new ConfigScreenFactory(
				(client, parent) -> {
					if (Compat.isClothConfigLoaded()) {
						return AutoConfig.getConfigScreen(ChatHeadsConfigData.class, parent).get();
					} else {
						return new MissingClothConfigScreen(parent);
					}
				}));
	}
}
