package dzwdz.chat_heads;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.network.PlayerListEntry;
import org.jetbrains.annotations.Nullable;

public class EntryPoint implements ModInitializer {
    @Nullable
    public static PlayerListEntry lastSender;

    public static final int CHAT_OFFSET = 10;

    @Override
    public void onInitialize() {

    }
}
