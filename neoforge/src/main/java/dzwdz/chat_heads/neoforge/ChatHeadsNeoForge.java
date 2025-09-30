package dzwdz.chat_heads.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod("chat_heads")
public class ChatHeadsNeoForge {
	public ChatHeadsNeoForge(IEventBus modBus) {
		if (FMLEnvironment.getDist() == Dist.CLIENT) {
			ChatHeadsNeoForgeClient.init(modBus);
		}
	}
}
