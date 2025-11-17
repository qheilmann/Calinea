package io.calinea.measurer.components;

import io.calinea.Calinea;
import io.calinea.measurer.IComponentMeasurer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ScoreComponent;

public class ScoreComponentMeasurer implements IComponentMeasurer<ScoreComponent>{

    public ScoreComponentMeasurer() {}

    @Override
    public boolean canHandle(Component component) {
        return component instanceof ScoreComponent;
    }

    @Override
    public double measureRoot(ScoreComponent component) {

        // ScoreComponent are supposed to be resolved server-side, if not it will not be resolved client-side either and will show an empty TextComponent instead
        
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
}
