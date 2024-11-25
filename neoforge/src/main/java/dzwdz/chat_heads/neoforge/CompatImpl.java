package dzwdz.chat_heads.neoforge;

import net.neoforged.fml.loading.LoadingModList;

public class CompatImpl {
    public static boolean isModLoaded(String modId) {
        // ModList is null at the point where we need it
        return LoadingModList.get().getModFileById(modId) != null;
    }

    public static boolean isClothConfigLoaded() {
        return isModLoaded("cloth_config");
    }
}
