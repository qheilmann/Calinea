package io.calinea.resolver;

import java.util.Locale;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import io.calinea.pack.PackInfo;
import io.calinea.resolver.Client.ForcedClientComponentResolver;
import io.calinea.resolver.Server.ServerComponentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

public class ComponentResolver implements IComponentResolver{

    public static final String MINECRAFT_FALLBACK_LOCAL = "en_US".toLowerCase();

    private final PackInfo packInfo;

    public ComponentResolver(PackInfo packInfo) {
        this.packInfo = packInfo;
    }
    
    public Component resolve(ComponentLike componentLike, CommandSender context, Entity scoreboardSubject) {
        
        Component component = componentLike.asComponent();
        
        // Resolve server-side components
        ServerComponentResolver serverResolver = new ServerComponentResolver();
        component = serverResolver.resolve(component, context, scoreboardSubject);
        
        // Force resolution of client-side components
        
        Locale locale;
        if (context instanceof Player player) {
            locale = player.locale();
        } else {
            locale = Locale.of(MINECRAFT_FALLBACK_LOCAL);
        }

        ForcedClientComponentResolver forcedClientResolver = new ForcedClientComponentResolver(packInfo);
        component = forcedClientResolver.resolve(component, locale);

        return component;
    }
}
