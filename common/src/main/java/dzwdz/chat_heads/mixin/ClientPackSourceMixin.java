package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Mixin(ClientPackSource.class)
public abstract class ClientPackSourceMixin {
	@Shadow @Nullable
	private Pack serverPack;

	// note: runs on Netty Client IO thread
    @Inject(method = "setServerPack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;delayTextureReload()Ljava/util/concurrent/CompletableFuture;"))
    public void chatheads$checkForDisableResource(File file, PackSource packSource, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		if (serverPack == null) return; // never null

		try (PackResources resources = serverPack.open()) {
			try (var stream = resources.getResource(PackType.CLIENT_RESOURCES, ChatHeads.DISABLE_RESOURCE)) {
				ChatHeads.serverDisabledChatHeads = true;
			} catch (IOException ignored) {}
		}
    }
}
