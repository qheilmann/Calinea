package io.calinea.measurer.components;

import io.calinea.Calinea;
import io.calinea.measurer.ComponentMeasurerConfig;
import io.calinea.measurer.IComponentMeasurer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.format.TextDecoration;

public class SelectorComponentMeasurer implements IComponentMeasurer<SelectorComponent>{
    ComponentMeasurerConfig config;

    public SelectorComponentMeasurer(ComponentMeasurerConfig config) {
        this.config = config;
    }

    @Override
    public boolean canHandle(Component component) {
        return component instanceof SelectorComponent;
    }

    @Override
    public double measureRoot(SelectorComponent component) {
        
        // SelectorComponents should be resolved server-side. If not resolved,
        // the client will render the selector pattern as TextComponent directly,
        // so we measure the pattern string's width directly.
        String pattern = component.pattern();
        Key fontKey = component.font();
        boolean isBold = component.style().hasDecoration(TextDecoration.BOLD);

        TextComponentMeasurer textMeasurer = new TextComponentMeasurer(config);
        Double width = textMeasurer.measureTextWidth(pattern, fontKey, isBold);

        // Warn that an unresolved SelectorComponent is being measured
        if (Calinea.getConfig().warnOnUnresolvedServerComponents()) {
            Calinea.getLogger().warning(String.format("Unresolved SelectorComponent detected - '%s'. It should be resolved server-side before measurement. Falling back to the pattern itself (%.1f pixels). This may indicate that the component was not properly resolved before using the %s API.", pattern, width, Calinea.LIBRARY_NAME));
        }

        return width;
    }
}
