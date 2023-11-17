package dzwdz.chat_heads.forge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod("chat_heads")
public class ChatHeads {
	public ChatHeads() {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			ChatHeadsClient.init();
		}
	}
}
