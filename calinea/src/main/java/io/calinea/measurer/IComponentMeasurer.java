package io.calinea.measurer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

/**
 * Interface for specific component type measurers.
 * Each implementation handles measurement for a specific component type.
 */
public interface IComponentMeasurer<C extends ComponentLike> {

    /**
     * Measures the root width of the given component, excluding its children.
     */
    double measureRoot(C component);
    
    /**
     * Checks if this measurer can handle the given component type.
     */
    boolean canHandle(Component component);
}
