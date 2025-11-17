package io.calinea.measurer.components;

import io.calinea.Calinea;
import io.calinea.measurer.ComponentMeasurerConfig;
import io.calinea.measurer.IComponentMeasurer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.StorageNBTComponent;

public class StorageNBTComponentMeasurer implements IComponentMeasurer<StorageNBTComponent>{
    ComponentMeasurerConfig config;

    public StorageNBTComponentMeasurer(ComponentMeasurerConfig config) {
        this.config = config;
    }

    @Override
    public boolean canHandle(Component component) {
        return component instanceof StorageNBTComponent;
    }

    @Override
    public double measureRoot(StorageNBTComponent component) {

        // StorageNBTComponent are supposed to be resolved server-side, if not it will not be resolved client-side either and will show an empty TextComponent instead

        // Warn that an unresolved StorageNBTComponent is being measured
        if (Calinea.getConfig().warnOnUnresolvedServerComponents()) {
            Calinea.getLogger().warning(String.format("Unresolved StorageNBTComponent detected - '%s'. It should be resolved server-side before measurement. Falling back to an empty component. This may indicate that the component was not properly resolved before using the %s API.", component.storage(), Calinea.LIBRARY_NAME));
        }

        return 0;
    }
}
