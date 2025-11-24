package io.calinea.resolver.Server;

import net.kyori.adventure.text.Component;
import java.io.IOException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import io.calinea.Calinea;
import io.papermc.paper.text.PaperComponents;

public class ServerComponentResolver {

    public Component resolve(Component component, CommandSender context, Entity scoreboardSubject) {
        try {
            return PaperComponents.resolveWithContext(component, context, scoreboardSubject, true);
        } catch (IOException e) {
            Calinea.logger().severe("Failed to resolve the server-side Component: " + component, e);
        }
        return component;
    }
}
