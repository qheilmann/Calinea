package io.calinea.segmentation.handlers;

import io.calinea.Calinea;
import io.calinea.segmentation.measurer.ComponentMeasurerConfig;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Handler for SelectorComponent.
 * <p>
 * SelectorComponents should be resolved server-side. If not resolved, the client will render 
 * the selector pattern as TextComponent directly, so we measure the pattern string's width directly.
 */
public class SelectorComponentHandler implements IComponentLayoutHandler<SelectorComponent>{
    ComponentMeasurerConfig config;

    public SelectorComponentHandler(ComponentMeasurerConfig config) {
        this.config = config;
    }

    @Override
    public boolean canHandle(Component component) {
        return component instanceof SelectorComponent;
    }

    @Override
    public double measureRoot(SelectorComponent component) {
        // Use asTextComponent to get the text representation
        TextComponent textComponent = asTextComponent(component);
        String content = textComponent.content();
        Key fontKey = textComponent.font();
        boolean isBold = textComponent.style().hasDecoration(TextDecoration.BOLD);

        TextComponentHandler textMeasurer = new TextComponentHandler(config);
        Double width = textMeasurer.measureTextWidth(content, fontKey, isBold);

        // Warn that an unresolved SelectorComponent is being measured
        if (Calinea.getConfig().warnOnUnresolvedServerComponents()) {
            Calinea.getLogger().warning(String.format(
                "Unresolved SelectorComponent detected - '%s'. " +
                "It should be resolved server-side before measurement. " +
                "Falling back to the pattern itself (%.1f pixels). " +
                "This may indicate that the component was not properly resolved before using the %s API.",
                content, width, Calinea.LIBRARY_NAME));
        }

        return width;
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public TextComponent asTextComponent(SelectorComponent component) {
        return Component.text(component.pattern(), component.style());
    }
}
