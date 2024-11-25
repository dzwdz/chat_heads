package dzwdz.chat_heads.fabric;

import net.fabricmc.loader.api.FabricLoader;

public class CompatImpl {
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static boolean isClothConfigLoaded() {
        return isModLoaded("cloth-config2");
    }
}
