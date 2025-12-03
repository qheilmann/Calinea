package io.calinea.resolver.Client;

import java.util.Locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TranslatableComponent;
public class KeyBindComponentResolver implements IClientComponentResolver<KeybindComponent> {

    private final TranslatableComponentResolver translatableResolver;

    public KeyBindComponentResolver(TranslatableComponentResolver translatableResolver) {
        this.translatableResolver = translatableResolver;
    }

    @Override
    public boolean canResolve(Component component) {
        return component instanceof KeybindComponent;
    }

    /**
     * Resolves a KeybindComponent into a TextComponent using a TranslatableComponent.
     * This resolves the keybind key into its localized string representation based on the provided locale.
     * Example: "key.forward" -> "Walk Forward" (for English locale)
     * 
     * @param component The KeybindComponent to resolve
     * @param locale The locale to use for resolution
     * @return The resolved Component
     */
    @Override
    public Component resolve(KeybindComponent component, Locale locale) {
        TranslatableComponent translatable = Component.translatable(component.keybind(), component.style());
        Component resolved = translatableResolver.resolve(translatable, locale);
        return resolved.append(component.children());
    }
}
