package dzwdz.chat_heads.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod("chat_heads")
public class ChatHeadsForge {
	public ChatHeadsForge() {
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ChatHeadsForgeClient::init);
	}
}
