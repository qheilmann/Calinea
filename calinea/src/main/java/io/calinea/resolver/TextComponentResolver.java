package io.calinea.resolver;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class TextComponentResolver implements ComponentResolver<TextComponent, Void> {
    @Override
    public Component resolve(TextComponent component, Void context) {
        // Since a text component is already fully resolved, we can return it as is.
        return component;
    }
}
