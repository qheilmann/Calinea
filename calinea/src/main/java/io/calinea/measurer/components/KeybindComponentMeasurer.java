package io.calinea.measurer.components;

import io.calinea.Calinea;
import io.calinea.measurer.ComponentMeasurerConfig;
import io.calinea.measurer.IComponentMeasurer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.format.TextDecoration;

public class KeybindComponentMeasurer implements IComponentMeasurer<KeybindComponent>{
    ComponentMeasurerConfig config;

    public KeybindComponentMeasurer(ComponentMeasurerConfig config) {
        this.config = config;
    }

    @Override
    public boolean canHandle(Component component) {
        return component instanceof KeybindComponent;
    }

    @Override
    public double measureRoot(KeybindComponent component) {

        // KeybindComponents should be resolved server-side. If not resolved,
        // the client will render the correct keybind but the measurement can vary depending on the user's keybind settings.
        // so we measure the default keybind pattern instead and warn the consumer.
        // IMPROVEMENT: If it possible to have the default keybind settings, we could use that instead for more accurate measurements.
        String identifier = component.keybind();
        Key fontKey = component.font();
        boolean isBold = component.style().hasDecoration(TextDecoration.BOLD);

        TextComponentMeasurer textMeasurer = new TextComponentMeasurer(config);
        Double width = textMeasurer.measureTextWidth(identifier, fontKey, isBold);

        
        // Warn that an unresolved KeybindComponent is being measured
        if (Calinea.getConfig().warnOnUnforcedClientComponents()) {
            Calinea.getLogger().warning(String.format(
                "Unforced KeybindComponent '%s' detected. " + 
                "Keybind components should be forced server-side before measurement. " +
                "Falling back to the keybind identifier (%.1f pixels), " + 
                "which may produce inaccurate results depending on the user settings. " + 
                "This likely indicates the component wasn't forced before using the %s API.",
                identifier, width, Calinea.LIBRARY_NAME));
        }

        return width;
    }
}
