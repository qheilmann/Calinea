package io.calinea.segmentation.handlers;

import java.util.List;

import io.calinea.Calinea;
import io.calinea.segmentation.measurer.ComponentMeasurer;
import io.calinea.segmentation.measurer.ComponentMeasurerConfig;
import io.calinea.utils.TranslatableComponentUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Handler for TranslatableComponent.
 * <p>
 * TranslatableComponents should be resolved server-side. If not resolved, the client will render 
 * the correct translation but the measurement can vary depending on the user's locale settings.
 * To approximate the width, we default to English translation or the fallback, and warn the consumer.
 * <p>
 * For splitting, we flatten the component into a TextComponent structure (English fallback),
 * preserving arguments as children.
 */
public class TranslatableComponentHandler implements IComponentLayoutHandler<TranslatableComponent>{
    ComponentMeasurerConfig config;

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

    public TranslatableComponentHandler(ComponentMeasurerConfig config) {
        this.config = config;
    }

    @Override
    public boolean canHandle(Component component) {
        return component instanceof TranslatableComponent;
    }

    @Override
    public double measureRoot(TranslatableComponent component) {
        String identifier = component.key();
        Key fontKey = component.font();
        Style parentStyle = component.style();

        String translation = TranslatableComponentUtils.extractEnglishTranslation(component);
        int placeholderCount = TranslatableComponentUtils.countPlaceholders(translation);
        
        double clearedWidth = measureCleanTranslation(translation, fontKey, parentStyle);
        double totalArgsWidth = measureArguments(component, placeholderCount, parentStyle);
        
        double width = clearedWidth + totalArgsWidth;

        warnIfUnforcedComponent(identifier, translation, width);

        return width;
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public TextComponent asTextComponent(TranslatableComponent component) {
        return TranslatableComponentUtils.flattenInEnglish(component);
    }

    //     return placeholderCount;
    // }
    
    private double measureCleanTranslation(String translation, Key fontKey, Style parentStyle) {
        String cleanTranslation = translation.replace(PLACEHOLDER, ""); // remove placeholders for clean measurement
        
        TextComponentHandler textMeasurer = new TextComponentHandler(config);
        return textMeasurer.measureTextWidth(cleanTranslation, fontKey, parentStyle.hasDecoration(TextDecoration.BOLD));
    }
    
    private double measureArguments(TranslatableComponent component, int placeholderCount, Style parentStyle) {
        // Also get the width of each argument
        double[] totalArgsWidthArray = new double[]{0.0};
        List<TranslationArgument> args = adjustArgumentsList(component.arguments(), placeholderCount);
        
        args.forEach(arg -> {
            Component componentArgument = arg.asComponent();
            
            ComponentMeasurer measurer = new ComponentMeasurer(config);
            double argumentWidth = measurer.measure(componentArgument, parentStyle);
            
            totalArgsWidthArray[0] += argumentWidth;
        });
        
        return totalArgsWidthArray[0];
    }
    
    private List<TranslationArgument> adjustArgumentsList(List<TranslationArgument> args, int placeholderCount) {
        // If translatableComponent are missing arguments, all arguments will be considered as %s 
        // (currently we can't distinguish if the pattern contains %s or positional %1$s, %2$s)
        // If translatableComponent has more arguments than expected, the extra arguments are ignored
        if (args.size() < placeholderCount) {
            return DUMMY_ARGUMENTS.subList(0, placeholderCount);
        } else if (args.size() > placeholderCount) {
            return args.subList(0, placeholderCount);
        }
        return args;
    }
    
    private void warnIfUnforcedComponent(String identifier, String translation, double width) {
        // Warn that an unresolved TranslatableComponent is being measured
        if (Calinea.getConfig().warnOnUnforcedClientComponents()) {
            Calinea.getLogger().warning(String.format(
                "Unforced TranslatableComponent '%s' detected. " +
                "Translatable components should be forced server-side before measurement. " +
                "Falling back to the translation fallback ('%s':%.1f pixels), " +
                "which may produce inaccurate results depending on the user locale. " +
                "This likely indicates the translation wasn't forced before using the %s API.",
                identifier, translation, width, Calinea.LIBRARY_NAME));
        }
    }
}
