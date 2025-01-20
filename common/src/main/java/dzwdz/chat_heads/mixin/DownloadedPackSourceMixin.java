package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.resources.server.PackReloadConfig;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DownloadedPackSource.class)
public abstract class DownloadedPackSourceMixin {
    @Inject(method = "loadRequestedPacks", at = @At(value = "RETURN"))
    public void chatheads$checkForDisableResource(List<PackReloadConfig.IdAndPath> list, CallbackInfoReturnable<List<Pack>> cir) {
        List<Pack> packs = cir.getReturnValue();
        if (packs == null) return; // in the rare case the server pack is invalid

        for (Pack serverPack : packs) {
            try (PackResources resources = serverPack.open()) {
                if (resources.getResource(PackType.CLIENT_RESOURCES, ChatHeads.DISABLE_RESOURCE) != null) {
                    ChatHeads.serverDisabledChatHeads = true;
                    ChatHeads.LOGGER.info("Chat Heads disabled by server request");
                }
            }
        }
    }
}
