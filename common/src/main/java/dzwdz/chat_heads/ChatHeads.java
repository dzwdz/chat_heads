package dzwdz.chat_heads;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dzwdz.chat_heads.config.ChatHeadsConfig;
import dzwdz.chat_heads.config.ChatHeadsConfigDefaults;
import dzwdz.chat_heads.mixinterface.GuiMessageOwnerAccessor;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static dzwdz.chat_heads.config.SenderDetection.HEURISTIC_ONLY;
import static dzwdz.chat_heads.config.SenderDetection.UUID_ONLY;

/*
 * 22w42a changed chat a bit, here's the overview:
 *
 * previous ClientboundPlayerChatPacket was split into ClientboundPlayerChatPacket and ClientboundDisguisedChatPacket
 * (ChatListener.handleChatMessage() -> ClientPacketListener.handlePlayerChat() and ClientPacketListener.handleDisguisedChat())
 * "disguised player messages" are the equivalent of the previous "system signed player messages" which were player messages with UUID 0
 *
 * Call stack looks roughly like this:
 *
 * ClientPacketListener.handlePlayerChat()
 *  -> ChatListener.handlePlayerChatMessage(), note: doesn't take PlayerInfo but GameProfile instead
 *  -> ChatListener.showMessageToPlayer()
 *  -> ChatComponent.addMessage()
 *  -> new GuiMessage.Line()
 *
 * ClientPacketListener.handleDisguisedChat()
 *  -> ChatListener.handleDisguisedChatMessage()
 *  -> ChatComponent.addMessage()
 *  -> new GuiMessage.Line()
 *
 * ClientPacketListener.handleSystemChat()
 *  -> ChatListener.handleSystemMessage()
 *  -> ChatComponent.addMessage()
 *  -> new GuiMessage.Line()
 *
 * FreedomChat (https://github.com/Oharass/FreedomChat) will likely work the same as before, converting chat messages
 * to system messages, so we still handle those.
 */

public class ChatHeads {
    public static final String MOD_ID = "chat_heads";
    public static ChatHeadsConfig CONFIG = new ChatHeadsConfigDefaults();

    @Nullable
    public static PlayerInfo lastSender;
    @Nullable
    public static GuiMessage.Line lastGuiMessage;

    public static int lastY = 0;
    public static float lastOpacity = 0.0f;
    public static int lastChatOffset;
    public static boolean serverSentUuid = false;

    public static final Set<ResourceLocation> blendedHeadTextures = new HashSet<>();

    // requires ChatHeads.lastSender to be set beforehand because of good programming
    public static void handleAddedMessage(Component message) {
        if (ChatHeads.CONFIG.senderDetection() != HEURISTIC_ONLY) {
            if (ChatHeads.lastSender != null) {
                ChatHeads.serverSentUuid = true;
                return;
            }

            // no PlayerInfo/UUID, message is either not from a player or the server didn't wanna tell

            if (ChatHeads.CONFIG.senderDetection() == UUID_ONLY || ChatHeads.serverSentUuid && ChatHeads.CONFIG.smartHeuristics()) {
                return;
            }
        }

        // use heuristic to find sender
        ChatHeads.lastSender = ChatHeads.detectPlayer(message);
    }

    public static int getChatOffset(@NotNull GuiMessage.Line guiMessage) {
        PlayerInfo owner = ((GuiMessageOwnerAccessor) (Object) guiMessage).chatheads$getOwner();
        return getChatOffset(owner);
    }

    public static int getChatOffset(@Nullable PlayerInfo owner) {
        if (owner != null || ChatHeads.CONFIG.offsetNonPlayerText()) {
            return 10;
        } else {
            return 0;
        }
    }

    /** Heuristic to detect the sender of a message, needed if there's no sender UUID */
    @Nullable
    public static PlayerInfo detectPlayer(Component message) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        Map<String, PlayerInfo> nicknameCache = new HashMap<>();

        if (connection == null) {
            return null;
        }

        // check each word consisting only out of allowed player name characters
        for (String word : message.getString().split("(§.)|[^\\w]")) {
            if (word.isEmpty()) continue;

            // manually translate nickname to profile name (needed for non-displayname nicknames)
            word = CONFIG.getProfileName(word);

            // check if player name
            PlayerInfo player = connection.getPlayerInfo(word);
            if (player != null) return player;

            // check if nickname
            player = getPlayerFromNickname(word, connection, nicknameCache);
            if (player != null) return player;
        }

        return null;
    }

    // helper method for detectPlayer using an (initially empty) cache to speed up subsequent calls
    // this cache will either be full or empty after this method returns
    @Nullable
    private static PlayerInfo getPlayerFromNickname(String word, ClientPacketListener connection, Map<String, PlayerInfo> nicknameCache) {
        if (nicknameCache.isEmpty()) {
            for (PlayerInfo p : connection.getOnlinePlayers()) {
                // on vanilla servers this seems to always be null, apparently it can only be set via modifying
                // ServerPlayer.getTabListDisplayName() or sending an UPDATE_DISPLAY_NAME packet to the client
                Component displayName = p.getTabListDisplayName();

                if (displayName != null) {
                    String nickname = displayName.getString();

                    // found match, we are done
                    if (word.equals(nickname)) {
                        nicknameCache.clear(); // make sure to not leave the cache in an incomplete state
                        return p;
                    }

                    // fill cache for subsequent calls
                    nicknameCache.put(nickname, p);
                }
            }
        } else {
            // use prepared cache
            return nicknameCache.get(word);
        }

        return null;
    }

    public static NativeImage extractBlendedHead(NativeImage skin) {
        NativeImage head = new NativeImage(8, 8, false);

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int headColor = skin.getPixelRGBA(8 + x, 8 + y);
                int hatColor = skin.getPixelRGBA(40 + x, 8 + y);

                // blend layers together
                head.setPixelRGBA(x, y, headColor);
                head.blendPixel(x, y, hatColor);
            }
        }

        return head;
    }

    public static ResourceLocation getBlendedHeadLocation(ResourceLocation skinLocation) {
        return new ResourceLocation(ChatHeads.MOD_ID, skinLocation.getPath());
    }

    public static void renderChatHead(PoseStack matrixStack, int x, int y, PlayerInfo owner) {
        ResourceLocation skinLocation = owner.getSkinLocation();

        if (blendedHeadTextures.contains(skinLocation)) {
            RenderSystem.setShaderTexture(0, getBlendedHeadLocation(skinLocation));

            // draw head in one draw call, fixing transparency issues of the "vanilla" path below
            GuiComponent.blit(matrixStack, x, y, 8, 8, 0, 0, 8, 8, 8, 8);
        } else {
            RenderSystem.setShaderTexture(0, skinLocation);

            // draw base layer
            GuiComponent.blit(matrixStack, x, y, 8, 8, 8.0f, 8, 8, 8, 64, 64);
            // draw hat
            GuiComponent.blit(matrixStack, x, y, 8, 8, 40.0f, 8, 8, 8, 64, 64);
        }
    }
}
