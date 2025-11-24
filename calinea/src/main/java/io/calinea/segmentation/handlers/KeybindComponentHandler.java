package io.calinea.segmentation.handlers;

import io.calinea.Calinea;
import io.calinea.segmentation.measurer.ComponentMeasurerConfig;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Handler for KeybindComponent.
 * <p>
 * KeybindComponents should be resolved server-side. If not resolved, the client will render 
 * the correct keybind but the measurement can vary depending on the user's keybind settings.
 * We measure the default keybind pattern instead and warn the consumer.
 */
public class KeybindComponentHandler implements IComponentLayoutHandler<KeybindComponent>{
    ComponentMeasurerConfig config;

    public KeybindComponentHandler(ComponentMeasurerConfig config) {
        this.config = config;
    }

    @Override
    public boolean canHandle(Component component) {
        return component instanceof KeybindComponent;
    }

    @Override
    public double measureRoot(KeybindComponent component) {
        TextComponent textComponent = asTextComponent(component);
        String identifier = textComponent.content();
        Key fontKey = textComponent.font();
        boolean isBold = textComponent.style().hasDecoration(TextDecoration.BOLD);

        TextComponentHandler textMeasurer = new TextComponentHandler(config);
        Double width = textMeasurer.measureTextWidth(identifier, fontKey, isBold);

        
        // Warn that an unresolved KeybindComponent is being measured
        if (Calinea.config().warnOnUnforcedClientComponents()) {
            Calinea.logger().warning(String.format(
                "Unforced KeybindComponent '%s' detected. " + 
                "Keybind components should be forced server-side before measurement. " +
                "Falling back to the keybind identifier (%.1f pixels), " + 
                "which may produce inaccurate results depending on the user settings. " + 
                "This likely indicates the component wasn't forced before using the %s API.",
                identifier, width, Calinea.LIBRARY_NAME));
        }

        return width;
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public TextComponent asTextComponent(KeybindComponent component) {
        return Component.text(component.keybind(), component.style());
    }
}
