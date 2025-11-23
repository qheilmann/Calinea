package io.calinea.segmentation.handlers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.TextComponent;
import org.jspecify.annotations.Nullable;

import io.calinea.segmentation.measurer.ComponentMeasurerConfig;

/**
 * Handler for ObjectComponent.
 * <p>
 * An ObjectComponent is rendered as an 8×8 sprite.
 * The bold style has no effect on ObjectComponents.
 * No additional spacing is applied between glyphs.
 */
public class ObjectComponentHandler implements IComponentLayoutHandler<ObjectComponent>{
    ComponentMeasurerConfig config;

    public ObjectComponentHandler(ComponentMeasurerConfig config) {
        this.config = config;
    }

    @Override
    public boolean canHandle(Component component) {
        return component instanceof ObjectComponent;
    }

    @Override
    public double measureRoot(ObjectComponent component) {
        return 8;
    }

    @Override
    public boolean isAtomic() {
        return true;
    }

    @Override
    public @Nullable TextComponent asTextComponent(ObjectComponent component) {
        return null;
    }
}
