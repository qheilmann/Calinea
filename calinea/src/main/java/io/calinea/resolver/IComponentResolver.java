package io.calinea.resolver;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

public interface IComponentResolver {
    public Component resolve(ComponentLike componentLike, CommandSender context, Entity scoreboardSubject);
}
