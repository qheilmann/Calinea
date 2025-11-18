package io.calinea.measurer.components;

import io.calinea.Calinea;
import io.calinea.measurer.ComponentMeasurerConfig;
import io.calinea.measurer.IComponentMeasurer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextDecoration;

public class TranslatableComponentMeasurer implements IComponentMeasurer<TranslatableComponent>{
    ComponentMeasurerConfig config;

    public TranslatableComponentMeasurer(ComponentMeasurerConfig config) {
        this.config = config;
    }

    @Override
    public boolean canHandle(Component component) {
        return component instanceof TranslatableComponent;
    }

    @Override
    public double measureRoot(TranslatableComponent component) {

        // TranslatableComponents should be resolved server-side. If not resolved,
        // the client will render the correct translation but the measurement can vary depending on the user's locale settings.
        // so we measure the default translation instead and warn the consumer.
        String identifier = component.key();
        Key fontKey = component.font();
        boolean isBold = component.style().hasDecoration(TextDecoration.BOLD);
        String fallback = component.fallback();
        if (fallback == null) {
            fallback = identifier;
        }

        // TODO translate to en_us locale if possible
        // check for extra args to also count inside the translatable (resolved not resolved idk)
        // also check for missing args and surplus args
        // now only with/without fallback and identifier/ bad identifier is good, reste the happy translation now

        TextComponentMeasurer textMeasurer = new TextComponentMeasurer(config);
        Double width = textMeasurer.measureTextWidth(fallback, fontKey, isBold);

        // Warn that an unresolved TranslatableComponent is being measured
        if (Calinea.getConfig().warnOnUnforcedClientComponents()) {
            Calinea.getLogger().warning(String.format(
                "Unforced TranslatableComponent '%s' detected. " +
                "Translatable components should be forced server-side before measurement. " +
                "Falling back to the translation fallback ('%s':%.1f pixels), " +
                "which may produce inaccurate results depending on the user locale. " +
                "This likely indicates the component wasn't forced before using the %s API.",
                identifier, fallback, width, Calinea.LIBRARY_NAME));
        }

        return width;
    }
}
