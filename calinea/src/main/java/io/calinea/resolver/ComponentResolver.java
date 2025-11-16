package io.calinea.resolver;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;


/**
 * Resolves Adventure components of type T into another Components using context C.
 *
 * @param <T> The type of ComponentLike to resolve
 * @param <C> The type of context used during resolution
 */
public interface ComponentResolver<T extends ComponentLike, C> {
    /**
     * Resolves the given component into another Component using the provided context.
     *
     * @param component The component to resolve
     * @param context   The context to use during resolution
     * @return The resolved Component
     */
    Component resolve(T component, C context);
}
