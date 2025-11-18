package io.calinea.measurer.components;

import io.calinea.measurer.ComponentMeasurerConfig;
import io.calinea.measurer.IComponentMeasurer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;

public class ObjectComponentMeasurer implements IComponentMeasurer<ObjectComponent>{
    ComponentMeasurerConfig config;

    public ObjectComponentMeasurer(ComponentMeasurerConfig config) {
        this.config = config;
    }

    @Override
    public boolean canHandle(Component component) {
        return component instanceof ObjectComponent;
    }

    @Override
    public double measureRoot(ObjectComponent component) {

        // An ObjectComponent is rendered as an 8×8 sprite.
        // The bold style has no effect on ObjectComponents.
        // No additional spacing is applied between glyphs.
        return 8;
    }
}
