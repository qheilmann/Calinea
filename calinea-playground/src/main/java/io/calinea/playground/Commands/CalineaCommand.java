package io.calinea.playground.Commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ChatComponentArgument;
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
                .withOptionalArguments(new IntegerArgument("width"))
                .executes((sender, args) -> {
                    Component component = (Component) args.getOrDefault("component", Component.text("=== CALINEA TEST ==="));
                    int width = (int) args.getOrDefault("width", 320);
                    Component centered = Calinea.center(component, width);
                    sender.sendMessage(centered);
                })
            )

            // measure
            .withSubcommand(new CommandAPICommand("measure")
                .withArguments(new ChatComponentArgument("component"))
                .executes((sender, args) -> {
                    Component component = (Component) args.getOrDefault("component", Component.text("=== CALINEA TEST ==="));
                    int width = Calinea.measureWidth(component);

                    //Format and send message
                    Component result = Component.text()
                        .append(Component.text("Width of '", NamedTextColor.GRAY))
                        .append(component.hoverEvent(Component.text(component.toString()))) // Show the component and its content on hover
                        .append(Component.text("': ", NamedTextColor.GRAY))
                        .append(Component.text(width, NamedTextColor.GRAY, TextDecoration.BOLD))
                        .append(Component.text(" pixels", NamedTextColor.GRAY))
                        .build();

                    sender.sendMessage(result);
                })
            )

            // left
            .withSubcommand(new CommandAPICommand("left")
                .withOptionalArguments(new ChatComponentArgument("component"))
                .withOptionalArguments(new IntegerArgument("width"))
                .executes((sender, args) -> {
                    Component component = (Component) args.getOrDefault("component", Component.text("Left"));
                    int width = (int) args.getOrDefault("width", 100);
                    Component result = Calinea.alignLeft(component, width);
                    sender.sendMessage(result);
                })
            )

            // right
            .withSubcommand(new CommandAPICommand("right")
                .withOptionalArguments(new ChatComponentArgument("component"))
                .withOptionalArguments(new IntegerArgument("width"))
                .executes((sender, args) -> {
                    Component component = (Component) args.getOrDefault("component", Component.text("Right"));
                    int width = (int) args.getOrDefault("width", 100);
                    Component result = Calinea.alignRight(component, width);
                    sender.sendMessage(result);
                })
            )

            // align
            .withSubcommand(new CommandAPICommand("align")
                .withArguments(new MultiLiteralArgument("alignment", "left", "right", "center"))
                .withOptionalArguments(new ChatComponentArgument("component"))
                .withOptionalArguments(new IntegerArgument("width"))
                .executes((sender, args) -> {
                    String alignment = (String) args.getOrDefault("alignment", "left");
                    Component component = (Component) args.getOrDefault("component", Component.text("Text"));
                    int width = (int) args.getOrDefault("width", 100);
                    
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
                .withOptionalArguments(new IntegerArgument("width"))
                .executes((sender, args) -> {
                    int width = (int) args.getOrDefault("width", 200);
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
