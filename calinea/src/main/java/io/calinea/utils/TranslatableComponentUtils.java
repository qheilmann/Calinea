package io.calinea.utils;

import java.util.List;
import io.papermc.paper.text.PaperComponents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.flattener.FlattenerListener;

public class TranslatableComponentUtils {

    public final static String PLACEHOLDER = "%s";
    public final static Component DUMMY_PLACEHOLDER_COMPONENT = Component.text(PLACEHOLDER);
    private final static List<TranslationArgument> DUMMY_ARGUMENTS = List.of(
        TranslationArgument.component(DUMMY_PLACEHOLDER_COMPONENT),
        TranslationArgument.component(DUMMY_PLACEHOLDER_COMPONENT),
        TranslationArgument.component(DUMMY_PLACEHOLDER_COMPONENT),
        TranslationArgument.component(DUMMY_PLACEHOLDER_COMPONENT),
        TranslationArgument.component(DUMMY_PLACEHOLDER_COMPONENT),
        TranslationArgument.component(DUMMY_PLACEHOLDER_COMPONENT),
        TranslationArgument.component(DUMMY_PLACEHOLDER_COMPONENT),
        TranslationArgument.component(DUMMY_PLACEHOLDER_COMPONENT)
    );

    /**
     * Flattens the TranslatableComponent into a TextComponent which contains the English translation and the arguments as children.
     * If there are insufficient arguments, {@link #DUMMY_PLACEHOLDER_COMPONENT} will be used for ALL arguments.
     *
     * @param component The TranslatableComponent to flatten.
     * @return A TextComponent with the English translation and arguments inserted.
     */
    public static TextComponent flattenInEnglish(TranslatableComponent component) {
        String translation = extractEnglishTranslation(component);

        String[] parts = translation.split("%s");
        int numberOfPlaceholders = parts.length - 1;
        List<Component> arguments = component.arguments().stream()
            .map(c -> c.asComponent())
            .toList();
        
        // Not enough arguments to fill all placeholders -> replace all by the placeholder
        boolean insufficientArguments = false;
        if (arguments.size() < numberOfPlaceholders) {
            insufficientArguments = true;
        }

        // Apply parts and arguments
        TextComponent translationPart = Component.empty();
        for (int i = 0; i < numberOfPlaceholders; i++) {
            String part = parts[i];
            translationPart = translationPart.append(Component.text(part), getPlaceholder(insufficientArguments, arguments, i));
        }

        // Append the last part
        translationPart = translationPart.append(Component.text(parts[parts.length - 1]));

        // Then append the child (in a separate node)
        TextComponent childrenPart = Component.empty().append(component.children());

        // Merge both and add the style of the original component
        TextComponent result = Component.empty().style(component.style())
            .append(translationPart)
            .append(childrenPart);

        return result;
    }

    private static Component getPlaceholder(boolean insufficientArguments, List<Component> arguments, int argIndex) {
        if (!insufficientArguments) {
            return arguments.get(argIndex);
        }
        return DUMMY_PLACEHOLDER_COMPONENT;
    }

    /**
     * Extracts the English translation from a TranslatableComponent, removing any children.
     * and replacing arguments with {@link #DUMMY_PLACEHOLDER_COMPONENT}.
     *
     * @param component The TranslatableComponent to extract from.
     * @return The English translation as a String.
     */
    public static String extractEnglishTranslation(TranslatableComponent component) {
        component = component.children(List.of()); // Clear children, (calculated separately)
        component = component.arguments(DUMMY_ARGUMENTS); // Place dummy arguments for argument counting afterwards

        StringBuilder translatedText = new StringBuilder();
        PaperComponents.flattener()
            .flatten(component, new FlattenerListener() {
                @Override
                public void component(String text) {
                    translatedText.append(text);
                }
            });
        
        return translatedText.toString();
    }

    public static int countPlaceholders(String translation) {
        // Count how many arguments are in the translation
        int placeholderCount = 0;
        int index = 0;
        while ((index = translation.indexOf(PLACEHOLDER, index)) != -1) {
            placeholderCount++;
            index += PLACEHOLDER.length();
        }

        return placeholderCount;
    }
}
