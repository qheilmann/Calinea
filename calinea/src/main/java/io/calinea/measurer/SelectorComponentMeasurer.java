package io.calinea.measurer;

import io.calinea.Calinea;
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
        
        // SelectorComponent are supposed to be resolved server-side, if not it will not be resolved client-side either and will show the pattern instead like a TextComponent
        String pattern = component.pattern();
        Key fontKey = component.font();
        boolean isBold = component.style().hasDecoration(TextDecoration.BOLD);
        
        TextComponentMeasurer textMeasurer = new TextComponentMeasurer(config);
        Double width = textMeasurer.measureTextWidth(pattern, fontKey, isBold);

        // Warn that an unresolved SelectorComponent is being measured
        if (Calinea.getConfig().warnOnUnresolvedSelectorComponents()) {
            Calinea.getLogger().warning(String.format("Unresolved SelectorComponent detected - '%s' should be resolved server-side before measurement. Falling back to pattern text width: %.1f pixels. This may indicate the component was not properly resolved by the API consumer.", pattern, width));
        }

        return width;
    }
}
