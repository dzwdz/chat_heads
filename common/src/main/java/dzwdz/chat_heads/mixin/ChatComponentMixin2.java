package dzwdz.chat_heads.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.ChatHeads.PlayerInfoCache;
import dzwdz.chat_heads.HeadData;
import dzwdz.chat_heads.config.RenderPosition;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// needs to apply after Compact Chat's refreshTrimmedMessage() and recursive addMessage() call
// also after Chat Timestamps so the head position is correct
@Mixin(value = ChatComponent.class, priority = 10100)
public abstract class ChatComponentMixin2 {
    @Inject(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V",
            at = @At("HEAD")
    )
    private void chatheads$setRefreshing(Component chatComponent, MessageSignature headerSignature, int addedTime, GuiMessageTag tag, boolean onlyTrim, CallbackInfo ci, @Local(argsOnly = true) boolean refreshing) {
        ChatHeads.refreshing = refreshing;
        ChatHeads.lineData = ChatHeads.lastSenderData; // only really need to set when not refreshing
    }

    @Inject(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
        at = @At("RETURN")
    )
    private void chatheads$forgetSenderData(CallbackInfo ci) {
        ChatHeads.lastSenderData = HeadData.EMPTY;
    }

    @ModifyArg(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/ComponentRenderUtils;wrapComponents(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/client/gui/Font;)Ljava/util/List;"
        )
    )
    private FormattedText chatheads$updateHeadPosition(FormattedText component, @Local(argsOnly = true) boolean refreshing) {
        if (ChatHeads.CONFIG.renderPosition() != RenderPosition.BEFORE_NAME)
            return component;

        HeadData lineData = ChatHeads.getLineData();
        var connection = Minecraft.getInstance().getConnection();

        if (lineData != HeadData.EMPTY && connection != null) {
            // find head position for given player
            var playerInfoCache = new PlayerInfoCache(connection);
            playerInfoCache.add(lineData.playerInfo());
            HeadData foundHeadData = ChatHeads.scanForPlayerName(component.getString(), playerInfoCache);

            if (foundHeadData == HeadData.EMPTY) {
                ChatHeads.LOGGER.warn("couldn't find player name inside chat message");
            }

            if (foundHeadData.hasHeadPosition()) {
                // assert lineData.playerInfo().getProfile().getName().equals(foundHeadData.playerInfo().getProfile().getName())

                ChatHeads.setLineData(foundHeadData);
            }
        }

        return component;
    }
}
