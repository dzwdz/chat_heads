package dzwdz.chat_heads.forge;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
	public static boolean isModInstalled(String modId) {
		// ModList seems to always be null when shouldApplyMixin is executed
		// While not ideal, we can check which mods are loading
		for (ModInfo mod : FMLLoader.getLoadingModList().getMods())
			if (mod.getModId().equals(modId))
				return true;

		return false;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.endsWith("QuarkShareItemS2CMessageMixin") || mixinClassName.endsWith("QuarkItemSharingModuleMixin") ) {
			return isModInstalled("quark");
		}

		return true;
	}

	@Override
	public void onLoad(String mixinPackage) {

	}

	@Override
	public String getRefMapperConfig() {
		return "";
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return List.of();
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
