package io.calinea.resolver;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.SelectorComponent;

import java.io.IOException;

import org.bukkit.command.CommandSender;

import io.calinea.Calinea;
import io.papermc.paper.text.PaperComponents;

public class SelectorComponentResolver implements ComponentResolver<SelectorComponent, CommandSender> {
    @Override
    public Component resolve(SelectorComponent component, CommandSender context) {
        try {
            return PaperComponents.resolveWithContext(component, context, null, true);
        } catch (IOException e) {
            Calinea.getLogger().severe("Failed to resolve SelectorComponent: " + component, e);
        }
        return component;
    }
}
