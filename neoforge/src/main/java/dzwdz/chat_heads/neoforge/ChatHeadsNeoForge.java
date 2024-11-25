package dzwdz.chat_heads.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod("chat_heads")
public class ChatHeadsNeoForge {
	public ChatHeadsNeoForge() {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			ChatHeadsNeoForgeClient.init();
		}
	}
}
