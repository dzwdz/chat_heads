package dzwdz.chat_heads;

import net.fabricmc.api.ModInitializer;
import net.minecraft.text.StringRenderable;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EntryPoint implements ModInitializer {
    // yes, this is dumb
    @Nullable
    public static UUID lastUUID;
    @Nullable
    public static StringRenderable lastText;

    @Override
    public void onInitialize() {

    }
}
