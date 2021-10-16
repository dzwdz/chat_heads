package dzwdz.chat_heads.fabric;

import dzwdz.chat_heads.config.ChatHeadsConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import static dzwdz.chat_heads.ChatHeads.CONFIG;

public class ChatHeads implements ModInitializer {
	@Override
	public void onInitialize() {
		CONFIG = AutoConfig.register(ChatHeadsConfig.class, JanksonConfigSerializer::new).getConfig();
	}
}
