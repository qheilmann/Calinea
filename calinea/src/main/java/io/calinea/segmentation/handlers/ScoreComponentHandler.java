package io.calinea.segmentation.handlers;

import io.calinea.Calinea;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ScoreComponent;

/**
 * Handler for ScoreComponent.
 * <p>
 * ScoreComponents are supposed to be resolved server-side. If not, they will not be resolved 
 * client-side either and will show an empty TextComponent instead.
 */
public class ScoreComponentHandler implements IComponentLayoutHandler<ScoreComponent>{

    public ScoreComponentHandler() {}

    @Override
    public boolean canHandle(Component component) {
        return component instanceof ScoreComponent;
    }

    @Override
    public double measureRoot(ScoreComponent component) {

        // Warn that an unresolved ScoreComponent is being measured
        if (Calinea.getConfig().warnOnUnresolvedServerComponents()) {
            Calinea.getLogger().warning(String.format(
                "Unresolved ScoreComponent detected - '%s'. " +
                "It should be resolved server-side before measurement. " +
                "Falling back to an empty component. " +
                "This may indicate that the component was not properly resolved before using the %s API.",
                component.name(), Calinea.LIBRARY_NAME));
        }

        return 0;
    }

    @Override
    public boolean isAtomic() {
        return true;
    }

    @Override
    public TextComponent asTextComponent(ScoreComponent component) {
        return Component.empty().style(component.style());
    }
}
