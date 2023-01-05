package dzwdz.chat_heads.config;

import me.shedaniel.autoconfig.gui.registry.api.GuiProvider;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.StringListListEntry;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AliasesGuiProvider implements GuiProvider {
    /** Match "(nickname1 nickname2 ...) -> (profilename)" */
    public static Pattern PATTERN = Pattern.compile("\\s*(.+)\\s+->\\s+(\\S+)\\s*");

    /** Convert a list of "nick1 nick2 -> profilename" strings to a map from nicks to profile names */
    public static Map<String, String> toAliases(List<String> aliasStrings) {
        Map<String, String> aliases = new LinkedHashMap<>();

        for (String s : aliasStrings) {
            Matcher matcher = PATTERN.matcher(s);
            if (matcher.matches()) {
                String[] nicknames = matcher.group(1).split("\\s+");
                String profileName = matcher.group(2);

                for (String nick : nicknames) {
                    aliases.put(nick, profileName);
                }
            } else throw new IllegalArgumentException();
        }

        return aliases;
    }

    public static List<String> toStrings(Map<String, String> aliases) {
        Map<String, Set<String>> reverse = new LinkedHashMap<>(); // profile name -> nicknames

        for (var entry : aliases.entrySet()) {
            String nickname = entry.getKey();
            String profileName = entry.getValue();

            reverse.compute(profileName, (k, v) -> {
                if (v == null) v = new LinkedHashSet<>();
                v.add(nickname);
                return v;
            });
        }

        return reverse.entrySet().stream()
                .map(entry -> {
                    String profilename = entry.getKey();
                    String nicks = entry.getValue().stream().reduce((a, b) -> a + " " + b).get();
                    return nicks + " -> " + profilename;
                })
                .collect(Collectors.toList());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<AbstractConfigListEntry> get(String i13n, Field field, Object config, Object defaults, GuiRegistryAccess registry) {
        return Collections.singletonList(
            ConfigEntryBuilder.create()
                    .startStrList(Component.translatable(i13n), toStrings(Utils.getUnsafely(field, config)))
                    .setExpanded(true)
                    .setCreateNewInstance(entry -> new StringListListEntry.StringListCell("   ->   ", entry))
                    .setDefaultValue(() -> toStrings(Utils.getUnsafely(field, defaults)))
                    .setErrorSupplier(newValue -> {
                        try {
                            toAliases(newValue);
                            return Optional.empty();
                        } catch (Exception e) {
                            return Optional.of(Component.empty());
                        }
                    })
                    .setSaveConsumer(newValue -> Utils.setUnsafely(field, config, toAliases(newValue)))
                    .build()
        );
    }
}
