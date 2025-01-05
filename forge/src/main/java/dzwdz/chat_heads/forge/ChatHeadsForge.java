package dzwdz.chat_heads.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("chat_heads")
public class ChatHeadsForge {
	public ChatHeadsForge(FMLJavaModLoadingContext context) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ChatHeadsForgeClient.init(context));
	}
}
