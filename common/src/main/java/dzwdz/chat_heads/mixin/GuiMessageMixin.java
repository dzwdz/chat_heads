package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.mixininterface.Ownable;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.class)
public abstract class GuiMessageMixin implements Ownable {
    @Unique @Nullable
    public PlayerInfo chatheads$owner;

    @Override
    public void chatheads$setOwner(PlayerInfo owner) {
        chatheads$owner = owner;
    }

    @Override
    public PlayerInfo chatheads$getOwner() {
        return chatheads$owner;
    }
}
