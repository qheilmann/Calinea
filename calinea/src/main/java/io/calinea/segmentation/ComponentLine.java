package io.calinea.segmentation;

import net.kyori.adventure.text.Component;

public class ComponentLine {

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
}
