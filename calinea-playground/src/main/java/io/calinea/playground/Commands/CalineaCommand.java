package io.calinea.playground.Commands;

import java.util.List;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.ChatComponentArgument;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import io.calinea.Calinea;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class CalineaCommand {
    public static final String NAME = "calinea";
    public static final String[] ALIASES = {};
    public static final CommandPermission PERMISSION = CommandPermission.OP;
    public static final String SHORT_HELP = "Calinea library testing commands.";
    public static final String LONG_HELP = "Commands for testing various Calinea library features like centering, measuring, alignment, and separators";
    public static final String USAGE = """
                                    /calinea center [component] [width]
                                    /calinea measure [component]
                                    /calinea left [component] [width]
                                    /calinea right [component] [width]
                                    /calinea align (left|right|center) [component] [width]
                                    /calinea separator [width]
                                    /calinea reload
                                    """;

    private CalineaCommand() {
    }

    public static void register() {
        
        new CommandAPICommand(NAME)
            .withAliases(ALIASES)
            .withPermission(PERMISSION)
            .withHelp(SHORT_HELP, LONG_HELP)
            .withUsage(USAGE)
            
            // center
            .withSubcommand(new CommandAPICommand("center")
                .withOptionalArguments(new ChatComponentArgument("component"))
                .withOptionalArguments(new DoubleArgument("width"))
                .executes((sender, args) -> {
                    Component component = (Component) args.getOrDefault("component", Component.text("=== CALINEA TEST ==="));
                    double width = (double) args.getOrDefault("width", 320.0);
                    Component centered = Calinea.center(component, width);
                    sender.sendMessage(centered);
                })
            )

            // measure
            .withSubcommand(new CommandAPICommand("measure")
                .withArguments(new ChatComponentArgument("component"))
                .withOptionalArguments(new BooleanArgument("mustBeResolved"))
                .executesPlayer((player, args) -> {
                    Component component = (Component) args.getOrDefault("component", Component.text("=== CALINEA TEST ==="));
                    boolean mustBeResolved = (boolean) args.getOrDefault("mustBeResolved", false);

                    // Resolve
                    if (mustBeResolved) {
                        component = Calinea.resolve(component, player, player);
                    }

                    // Measure
                    double width = Calinea.measure(component);

                    //Format and send message
                    Component result = Component.text()
                        .append(Component.text("Width of '", NamedTextColor.GRAY))
                        .append(component.hoverEvent(Component.text(component.toString()))) // Show the component and its content on hover
                        .append(Component.text("': ", NamedTextColor.GRAY))
                        .append(Component.text(width, NamedTextColor.GRAY, TextDecoration.BOLD))
                        .append(Component.text(" pixels", NamedTextColor.GRAY))
                        .build();

                    player.sendMessage(result);
                })
            )

            .withSubcommand(new CommandAPICommand("split")
                .withArguments(new ChatComponentArgument("component"))
                .withOptionalArguments(new IntegerArgument("maxWidth"))
                .withOptionalArguments(new BooleanArgument("mustBeResolved"))
                .executesPlayer((player, args) -> {
                    Component component = (Component) args.get("component");
                    int maxWidth = (int) args.getOrDefault("maxWidth", 320);
                    boolean mustBeResolved = (boolean) args.getOrDefault("mustBeResolved", false);

                    // Resolve
                    if (mustBeResolved) {
                        component = Calinea.resolve(component, player, player);
                    }

                    // Split
                    List<Component> splits = Calinea.split(component, maxWidth).components();

                    // Send each split part
                    for (var part : splits) {
                        player.sendMessage(part.hoverEvent(Component.text(part.toString())));
                    }
                })
            )

            // left
            .withSubcommand(new CommandAPICommand("left")
                .withOptionalArguments(new ChatComponentArgument("component"))
                .withOptionalArguments(new DoubleArgument("width"))
                .executes((sender, args) -> {
                    Component component = (Component) args.getOrDefault("component", Component.text("Left"));
                    double width = (double) args.getOrDefault("width", 100.0);
                    Component result = Calinea.alignLeft(component, width);
                    sender.sendMessage(result);
                })
            )

            // right
            .withSubcommand(new CommandAPICommand("right")
                .withOptionalArguments(new ChatComponentArgument("component"))
                .withOptionalArguments(new DoubleArgument("width"))
                .executes((sender, args) -> {
                    Component component = (Component) args.getOrDefault("component", Component.text("Right"));
                    double width = (double) args.getOrDefault("width", 100.0);
                    Component result = Calinea.alignRight(component, width);
                    sender.sendMessage(result);
                })
            )

            // align
            .withSubcommand(new CommandAPICommand("align")
                .withArguments(new MultiLiteralArgument("alignment", "left", "right", "center"))
                .withOptionalArguments(new ChatComponentArgument("component"))
                .withOptionalArguments(new DoubleArgument("width"))
                .executes((sender, args) -> {
                    String alignment = (String) args.getOrDefault("alignment", "left");
                    Component component = (Component) args.getOrDefault("component", Component.text("Text"));
                    double width = (double) args.getOrDefault("width", 100.0);

                    Component result;
                    switch (alignment) {
                        case "left":
                            result = Calinea.alignLeft(component, width);
                            break;
                        case "right":
                            result = Calinea.alignRight(component, width);
                            break;
                        case "center":
                            result = Calinea.center(component, width);
                            break;
                        default:
                            result = Component.text("Invalid alignment: " + alignment);
                            break;
                    }
                    sender.sendMessage(result);
                })
            )

            // separator
            .withSubcommand(new CommandAPICommand("separator")
                .withOptionalArguments(new DoubleArgument("width"))
                .executes((sender, args) -> {
                    double width = (double) args.getOrDefault("width", 200.0);
                    Component separator = Calinea.separator(width);
                    sender.sendMessage(separator);
                })
            )

            // reload
            .withSubcommand(new CommandAPICommand("reload")
                .executes((sender, args) -> {
                    Calinea.reloadFonts();
                    sender.sendMessage(Component.text("Calinea fonts reloaded."));
                })
            )

            .register();
    }
}
