package dzwdz.chat_heads.mixin;

import dzwdz.chat_heads.ChatHeads;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    // note: can run on different threads
    @Inject(method = "initiateServerboundPlayConnection", at = @At("HEAD"))
    public void chatheads$resetServerKnowledge(String string, int i, ClientLoginPacketListener clientLoginPacketListener, CallbackInfo ci) {
        // reset every time we build a connection, be it singleplayer or multiplayer
        ChatHeads.serverSentUuid = false;
        ChatHeads.serverDisabledChatHeads = false;
    }
}
