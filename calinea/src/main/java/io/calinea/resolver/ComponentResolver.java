package io.calinea.resolver;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import io.calinea.resolver.Client.ForcedClientComponentResolver;
import io.calinea.resolver.Server.ServerComponentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

public class ComponentResolver implements IComponentResolver{
    
    public Component resolve(ComponentLike componentLike, CommandSender context, Entity scoreboardSubject) {
        
        Component component = componentLike.asComponent();
        
        // Resolve server-side components
        ServerComponentResolver serverResolver = new ServerComponentResolver();
        component = serverResolver.resolve(component, context, scoreboardSubject);
        
        // Force resolution of client-side components
        ForcedClientComponentResolver forcedClientResolver = new ForcedClientComponentResolver();
        component = forcedClientResolver.resolve(component);

        return component;
    }
}
