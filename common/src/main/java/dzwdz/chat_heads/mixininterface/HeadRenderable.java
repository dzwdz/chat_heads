package dzwdz.chat_heads.mixininterface;

import dzwdz.chat_heads.HeadData;
import org.jetbrains.annotations.NotNull;

public interface HeadRenderable {
    @NotNull HeadData chatheads$getHeadData();
    void chatheads$setHeadData(@NotNull HeadData headData);
}
