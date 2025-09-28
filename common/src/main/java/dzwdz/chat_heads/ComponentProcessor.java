package dzwdz.chat_heads;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/*
 * Components have siblings which are also components.
 * A chat message might thus be structured like this:
 *
 * component - sibling1                         sibling2             sibling3
 *                  \ - sibling1.1 - sibling1.2      \ - sibling 2.1
 *
 * They are parsed/rendered from left to right as shown.
 * (Technically speaking, they form a directed tree, parsed in depth first order.)
 * With 1.21.9 ObjectContents are now a thing, which can render AtlasSprites and PlayerSprites (literal chat heads).
 *
 * Previously we'd just do component.getString(), find a player name at a codepoint index,
 * then render the chat head when the character of that index was to be rendered.
 *
 * Calling getString() on a component containing a PlayerSprite would produce a "[<player_name> head]" string, which breaks this approach.
 * While fixable, this was never the ideal solution in the first place.
 *
 * Instead, we want to parse this component tree to find the position of the player name and then
 * insert a new ObjectContents, aka the chat head, by replacing or adding components.
 *
 * Assumptions:
 *  (A1):  the only components a player name can appear in are (the combination of all) literals and translatables.
 *  (A2):  player names can appear within a consecutive sequence of literals.
 *         a name could e.g. consist two components <Pla><yer>, each with their own style
 *  (A3a): player names are never split up between a translatable and another component.
 *         it's e.g. never <Pla><translatable with args=[<yer>]>
 *  (A3b): player names are never split up over the arguments of a translatable.
 *         the default template is "<%s> %s" and there is no template containing "%s%s", i.e. there's always padding
 *  (A3c): the default template can only contain the player name in its first argument (see also (A5))
 *   note: we could even go as far as to only handle the default translatables like "chat.type.text", but for now we'll handle all of them.
 *  (A4):  if a PlayerSprite is encountered, it is probably a chat head of the sender.
 *         (whether this is reasonable, time will tell)
 *  (A5):  we only want to add one chat head (for now...?)
 */

@SuppressWarnings("CodeBlock2Expr")
public class ComponentProcessor {
    /**
     * Walk the component-sibling tree in depth first order (aka render order).
     */
    public static void walkTree(Component component, Consumer<Component> consumer) {
        consumer.accept(component);

        for (Component sibling : component.getSiblings()) {
            walkTree(sibling, consumer);
        }
    }

    /**
     * Turns the component-sibling tree into a flat list of components without siblings.
     * Note: <code>getSiblings()<code/> of the new components is mutable!
     */
    public static ArrayList<Component> split(Component component) {
        ArrayList<Component> components = new ArrayList<>();

        walkTree(component, c -> {
            var copy = c.plainCopy().setStyle(c.getStyle());
            components.add(copy);
        });

        return components;
    }

    /**
     * Joins split components back together, i.e. <code>join(split(component))</code> renders the same as <code>component</code>.
     */
    public static Component join(List<Component> components) {
        if (components.isEmpty())
            return Component.empty();

        Component combined = components.getFirst();

        components.stream().skip(1).forEach(c -> {
            combined.getSiblings().add(c);
        });

        return combined;
    }

    public record FoundLiteralSequence(String text, int startIndex, int endIndex) {}

    public record FoundTranslatable(TranslatableContents contents, int index) {}

    // (A2) and (A3a)
    @SuppressWarnings("UnnecessaryReturnStatement")
    public static void walkLiteralSequencesAndTranslatables(ArrayList<Component> components,
            Predicate<FoundLiteralSequence> literalsCallback, Predicate<FoundTranslatable> translatableCallback) {
        // represents a sequence of consecutive literals
        StringBuilder textSequence = new StringBuilder();
        int startIndex = -1;
        int endIndex = -1; // inclusive

        for (int i = 0; i < components.size(); i++) {
            var contents = components.get(i).getContents();

            if (contents instanceof PlainTextContents textContents) {
                if (startIndex == -1) {
                    startIndex = i;
                }
                endIndex = i;

                textSequence.append(textContents.text());
            } else {
                if (!textSequence.isEmpty()) {
                    if (literalsCallback.test(new FoundLiteralSequence(textSequence.toString(), startIndex, endIndex)))
                        return;
                }

                textSequence = new StringBuilder();
                startIndex = -1;

                if (contents instanceof TranslatableContents translatableContents) {
                    if (translatableCallback.test(new FoundTranslatable(translatableContents, i)))
                        return;
                }
            }
        }

        endIndex = components.size() - 1;

        if (!textSequence.isEmpty()) {
            if (literalsCallback.test(new FoundLiteralSequence(textSequence.toString(), startIndex, endIndex)))
                return;
        }
    }

    public static String codePointSubstring(String string, int start) {
        return string.substring(string.offsetByCodePoints(0, start));
    }

    public static String codePointSubstring(String string, int start, int end) {
        int i = string.offsetByCodePoints(0, start);
        int j = string.offsetByCodePoints(i, end - start);
        return string.substring(i, j);
    }

    /**
     * Splits a literal into two at codepoint index i
     */
    @Nullable
    public static Pair<MutableComponent, MutableComponent> splitLiteral(Component literal, int i) {
        if (i == 0)
            return null;

        String text = ((PlainTextContents) literal.getContents()).text();

        String leftText  = codePointSubstring(text, 0, i);
        String rightText = codePointSubstring(text, i);

        var left  = Component.literal( leftText).setStyle(literal.getStyle());
        var right = Component.literal(rightText).setStyle(literal.getStyle());
        literal.getSiblings().forEach(right::append); // we don't use this, but might as well

        return new Pair<>(left, right);
    }

    @Nullable
    public static PlayerInfo addChatHeadForClickTellCommand(ArrayList<Component> components, ChatHeads.PlayerInfoCache playerInfoCache) {
        for (int i = 0; i < components.size(); i++) {
            var c = components.get(i);

            String profileName = getTellReceiver(c);
            if (profileName != null) {
                var playerInfo = playerInfoCache.get(profileName);

                if (playerInfo != null) {
                    var decorated = ComponentProcessor.createChatHeadComponent(playerInfo).append(c);
                    components.set(i, decorated);

                    // (A5)
                    return playerInfo;
                }
            }

            if (c.getContents() instanceof TranslatableContents translatable) {
                int finalI = i;

                var playerInfo = processTranslatableArguments(translatable, splitArg -> {
                    return addChatHeadForClickTellCommand(splitArg, playerInfoCache);
                }, decorated -> {
                    components.set(finalI, decorated);
                });

                // (A5)
                if (playerInfo != null)
                    return playerInfo;
            }
        }

        return null;
    }

    @Nullable
    public static <T> T processTranslatableArguments(TranslatableContents translatable, Function<ArrayList<Component>, @Nullable T> processSplitArg, Consumer<Component> processedCallback) {
        Object[] args = translatable.getArgs();

        // (A3c) for the default "<%s> %s" template, only check the first argument
        int argsLength = Objects.equals(translatable.getKey(), "chat.type.text") ? 1 : args.length;

        for (int i = 0; i < argsLength; i++) {
            if (args[i] instanceof String text) {
                args[i] = Component.literal(text);
            }

            if (args[i] instanceof Component argComponent) {
                // (A3b) treat each argument separately
                var splitArg = split(argComponent);
                T returnValue = processSplitArg.apply(splitArg);
                if (returnValue != null) {
                    Component processedArg = join(splitArg);

                    Object[] processedArgs = Arrays.copyOf(args, args.length);
                    processedArgs[i] = processedArg;

                    var processed = Component.translatableWithFallback(translatable.getKey(), translatable.getFallback(), processedArgs);

                    processedCallback.accept(processed);

                    // (A5)
                    return returnValue;
                }
            }
        }

        return null;
    }

    public static PlayerInfo addChatHeadForPlayerName(ArrayList<Component> components, ChatHeads.PlayerInfoCache playerInfoCache) {
        PlayerInfo[] returnValue = new PlayerInfo[1];

        walkLiteralSequencesAndTranslatables(components, literals -> {
            HeadData headData = ChatHeads.scanForPlayerName(literals.text, playerInfoCache);
            if (headData == HeadData.EMPTY)
                return false;

            // find the literal that contains the head position
            int codePointIndex = headData.codePointIndex();
            for (int i = literals.startIndex; i <= literals.endIndex; i++) {
                var literal = components.get(i);
                var contents = (PlainTextContents) literal.getContents();

                int codePointCount = (int) contents.text().replaceAll(ChatHeads.FORMAT_REGEX, "").codePoints().count();

                if (codePointIndex >= codePointCount) {
                    codePointIndex -= codePointCount;
                    continue;
                }

                // finally add the head
                var chatHead = ComponentProcessor.createChatHeadComponent(headData.playerInfo());
                Component decorated;

                var pair = splitLiteral(literal, codePointIndex);

                if (pair == null) {
                    decorated = chatHead.append(literal);
                } else {
                    var left = pair.getFirst();
                    var right = pair.getSecond();

                    decorated = left.append(chatHead).append(right);
                }

                components.set(i, decorated);

                // (A5)
                returnValue[0] = headData.playerInfo();
                return true;
            }

            return false;
        }, foundTranslatable -> {
            var translatable = foundTranslatable.contents;

            var foundPlayerInfo = processTranslatableArguments(translatable, splitArg -> {
                return addChatHeadForPlayerName(splitArg, playerInfoCache);
            }, decorated -> {
                components.set(foundTranslatable.index, decorated);
            });

            // (A5)
            if (foundPlayerInfo != null) {
                returnValue[0] = foundPlayerInfo;
                return true;
            }

            return false;
        });

        return returnValue[0];
    }

    public static boolean containsPlayerSprite(ArrayList<Component> components) {
        for (var c : components) {
            if (c.getContents() instanceof ObjectContents(ObjectInfo objectInfo) && objectInfo instanceof PlayerSprite) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    @SuppressWarnings({"UnnecessaryLocalVariable", "ConstantValue"})
    public static String getTellReceiver(Component component) {
        if (component.getStyle().getClickEvent() instanceof ClickEvent.SuggestCommand(String command)) {
            // can apparently be null, see issue #112
            if (command != null && command.startsWith("/tell ")) {
                String profileName = command.substring("/tell ".length()).trim();
                return profileName;
            }
        }

        return null;
    }

    public static MutableComponent createChatHeadComponent(PlayerInfo playerInfo) {
        return Component.object(new PlayerSprite(ResolvableProfile.createResolved(playerInfo.getProfile()), playerInfo.showHat()));
    }
}
