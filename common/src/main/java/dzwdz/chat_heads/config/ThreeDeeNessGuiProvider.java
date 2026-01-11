package dzwdz.chat_heads.config;

import dzwdz.chat_heads.ChatHeads;
import me.shedaniel.autoconfig.gui.registry.api.GuiProvider;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.world.item.component.ResolvableProfile;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class ThreeDeeNessGuiProvider implements GuiProvider {
    public static float lastSavedValue;

    @SuppressWarnings("rawtypes")
    @Override
    public List<AbstractConfigListEntry> get(String i13n, Field field, Object config, Object defaults, GuiRegistryAccess registry) {
        float currentValue = Utils.<Float>getUnsafely(field, config);
        lastSavedValue = currentValue;

        return Collections.singletonList(
                ConfigEntryBuilder.create()
                        .startLongSlider(Component.translatable(i13n), Math.round(currentValue * 100f), 0, 100)
                        .setTextGetter(v -> {
                            // temporarily set the value
                            ChatHeads.CONFIG.setThreeDeeNess(v / 100f);

                            var gameProfile = Minecraft.getInstance().getGameProfile();
                            var headWithHat = Component.object(new PlayerSprite(ResolvableProfile.createResolved(gameProfile), true));
                            return headWithHat.append(v + " %");
                        })
                        .setTooltip(Component.translatable(i13n + ".@Tooltip"))
                        .setDefaultValue(() -> (long) Math.round(Utils.<Float>getUnsafely(field, defaults) * 100f))
                        .setSaveConsumer(v -> {
                            float newValue = v / 100f;
                            Utils.setUnsafely(field, config, newValue);
                            lastSavedValue = newValue;
                        })
                        .build()
        );
    }
}


