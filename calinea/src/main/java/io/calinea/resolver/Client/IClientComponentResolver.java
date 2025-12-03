package io.calinea.resolver.Client;

import java.util.Locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;


/**
 * Resolves Adventure components of type T into another Components using context C.
 *
 * @param <T> The type of ComponentLike to resolve
 */
public interface IClientComponentResolver<T extends ComponentLike> {
    /**
     * Resolves the given component into another Component using the provided context.
     *
     * @param component The component to resolve
     * @return The resolved Component
     */
    Component resolve(T component, Locale locale);

    /**
     * Checks if this resolver can handle the given component.
     *
     * @param component The component to check
     * @return true if this resolver can handle the component, false otherwise
     */
    boolean canResolve(Component component);
}
