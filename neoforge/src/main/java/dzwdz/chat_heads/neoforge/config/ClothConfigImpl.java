package dzwdz.chat_heads.neoforge.config;

import dzwdz.chat_heads.Compat;
import dzwdz.chat_heads.config.ChatHeadsConfigData;
import dzwdz.chat_heads.config.MissingClothConfigScreen;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ClothConfigImpl {
	public static void registerConfigGui() {
		ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () ->
			(client, parent) -> {
					if (Compat.isClothConfigLoaded()) {
						return AutoConfigClient.getConfigScreen(ChatHeadsConfigData.class, parent).get();
					} else {
						return new MissingClothConfigScreen(parent);
					}
				});
	}
}
