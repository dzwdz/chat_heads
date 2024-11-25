package dzwdz.chat_heads;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class Compat {
    @ExpectPlatform
    public static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isClothConfigLoaded() {
        throw new AssertionError();
    }
}
