package io.calinea.segmentation.measurer;

import org.jspecify.annotations.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

/**
 * Main ComponentMeasurer that delegates to specific component measurers
 * based on the component type using polymorphism.
 */
public interface IComponentMeasurer {

    public double measure(ComponentLike componentLike);

    public double measure(ComponentLike componentLike, @Nullable Style parentStyle);

    public double measureRoot(ComponentLike componentLike);

    /**
     * Measures the width of a plain text string with the given style.
     * This is a helper method to avoid creating TextComponent objects just for measurement.
     */
    public double measureText(String text, Style style);

    /**
     * Converts a component to its TextComponent representation for splitting.
     * Delegates to the appropriate IComponentMeasurer.
     */
    public @Nullable TextComponent asTextComponent(Component component);

    /**
     * Checks if the component is atomic (cannot be split).
     */
    public boolean isAtomic(Component component);
}