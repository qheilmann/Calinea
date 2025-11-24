package io.calinea.resolver.Client;

import io.calinea.Calinea;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
public class KeyBindComponentResolver implements IClientComponentResolver<KeybindComponent> {

    @Override
    public boolean canResolve(Component component) {
        return component instanceof KeybindComponent;
    }
    
    @Override
    public Component resolve(KeybindComponent component) {

        if (Calinea.config().warnOnUnforcedClientComponents()) {
            Calinea.logger().info("KeybindComponent resolution is not fully supported yet, and can't be customized, it will be replaced with the keybind identifier: " + component.keybind());
        }

        return Component.text(component.keybind(), component.style()).append(component.children());
    }
}
