package io.calinea.measurer.components;

import io.calinea.Calinea;
import io.calinea.measurer.ComponentMeasurerConfig;
import io.calinea.measurer.IComponentMeasurer;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;

public class BlockNBTComponentMeasurer implements IComponentMeasurer<BlockNBTComponent>{
    ComponentMeasurerConfig config;

    public BlockNBTComponentMeasurer(ComponentMeasurerConfig config) {
        this.config = config;
    }

    @Override
    public boolean canHandle(Component component) {
        return component instanceof BlockNBTComponent;
    }

    @Override
    public double measureRoot(BlockNBTComponent component) {

        // BlockNBTComponent are supposed to be resolved server-side, if not it will not be resolved client-side either and will show an empty TextComponent instead

        // Warn that an unresolved BlockNBTComponent is being measured
        if (Calinea.getConfig().warnOnUnresolvedServerComponents()) {
            Calinea.getLogger().warning(String.format("Unresolved BlockNBTComponent detected - position:\"%s\". It should be resolved server-side before measurement. Falling back to an empty component. This may indicate that the component was not properly resolved before using the %s API.", component.pos(), Calinea.LIBRARY_NAME));
        }

        return 0;
    }
}
