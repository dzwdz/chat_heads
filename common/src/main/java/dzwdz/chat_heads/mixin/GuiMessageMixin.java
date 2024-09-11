package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.HeadData;
import dzwdz.chat_heads.mixininterface.HeadRenderable;
import net.minecraft.client.GuiMessage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.class)
public abstract class GuiMessageMixin implements HeadRenderable {
    @Unique @NotNull
    public HeadData chatheads$headData = HeadData.EMPTY;

    @Override
    public void chatheads$setHeadData(HeadData headData) {
        chatheads$headData = headData;
    }

    @Override @NotNull
    public HeadData chatheads$getHeadData() {
        return chatheads$headData;
    }
}
