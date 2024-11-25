package dzwdz.chat_heads.forge;

import net.minecraftforge.fml.loading.FMLLoader;

public class CompatImpl {
    public static boolean isModLoaded(String modId) {
        // ModList is null at the point where we need it
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    public static boolean isClothConfigLoaded() {
        return isModLoaded("cloth_config");
    }
}
