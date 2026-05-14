package io.calinea.segmentation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

public class ComponentLine implements ComponentLike {

    private final Component component;
    private final Double width;

    public ComponentLine(Component component, double width) {
        this.component = component;
        this.width = width;
    }

    public Component component() {
        return component;
    }

    public double width() {
        return width;
    }

    @Override
    public Component asComponent() {
        return component();
    }
}
