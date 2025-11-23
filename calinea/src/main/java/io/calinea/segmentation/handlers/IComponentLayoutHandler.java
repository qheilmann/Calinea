package io.calinea.segmentation.handlers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jspecify.annotations.Nullable;

/**
 * Interface for specific component layout handlers.
 * Each implementation handles layout logic (measuring, atomicity, text conversion) for a specific component type.
 */
public interface IComponentLayoutHandler<C extends Component> {

    /**
     * Measures the root width of the given component, excluding its children.
     */
    double measureRoot(C component);
    
    /**
     * Checks if this handler can handle the given component type.
     */
    boolean canHandle(Component component);

    /**
     * Checks if the component is atomic (cannot be split).
     * Atomic components are measured as a whole block.
     * Non-atomic components (like TextComponent) can be split into smaller parts.
     */
    boolean isAtomic();

    /**
     * Converts the component to a TextComponent representation for splitting purposes.
     * If the component is already a TextComponent, it should return itself.
     * For other components, it should return a TextComponent that represents the content
     * (e.g. the fallback text for a TranslatableComponent).
     * Returns null if the component has no text representation (e.g. ObjectComponent).
     */
    @Nullable TextComponent asTextComponent(C component);
}
