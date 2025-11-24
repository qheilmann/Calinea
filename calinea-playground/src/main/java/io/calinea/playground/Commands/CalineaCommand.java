package io.calinea.playground.Commands;

import java.util.List;

import org.bukkit.entity.Entity;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.ChatComponentArgument;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import io.calinea.Calinea;
import io.calinea.layout.Alignment;
import io.calinea.layout.LayoutBuilder;
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
                                    /calinea center [component] [width] [mustBeResolved]
                                    /calinea measure [component] [mustBeResolved]
                                    /calinea left [component] [width] [mustBeResolved]
                                    /calinea right [component] [width] [mustBeResolved]
                                    /calinea align (left|right|center) [component] [width] [mustBeResolved]
                                    /calinea separator [width] [mustBeResolved]
                                    /calinea reload
                                    """;

    private static final boolean DEFAULT_MUST_BE_RESOLVED = false;
    private static final double DEFAULT_WIDTH = 320.0; // default chat width

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
                .withOptionalArguments(new BooleanArgument("mustBeResolved"))
                .executesPlayer((player, args) -> {
                    Component component = (Component) args.getOrDefault("component", Component.text("=== CALINEA TEST ==="));
                    double width = (double) args.getOrDefault("width", DEFAULT_WIDTH);
                    boolean mustBeResolved = (boolean) args.getOrDefault("mustBeResolved", DEFAULT_MUST_BE_RESOLVED);

                    alignAndSend(player, component, Alignment.CENTER, width, mustBeResolved);
                })
            )

            // left
            .withSubcommand(new CommandAPICommand("left")
                .withOptionalArguments(new ChatComponentArgument("component"))
                .withOptionalArguments(new DoubleArgument("width"))
                .withOptionalArguments(new BooleanArgument("mustBeResolved"))
                .executesPlayer((player, args) -> {
                    Component component = (Component) args.getOrDefault("component", Component.text("Left"));
                    double width = (double) args.getOrDefault("width", DEFAULT_WIDTH);
                    boolean mustBeResolved = (boolean) args.getOrDefault("mustBeResolved", DEFAULT_MUST_BE_RESOLVED);

                    alignAndSend(player, component, Alignment.LEFT, width, mustBeResolved);
                })
            )

            // right
            .withSubcommand(new CommandAPICommand("right")
                .withOptionalArguments(new ChatComponentArgument("component"))
                .withOptionalArguments(new DoubleArgument("width"))
                .withOptionalArguments(new BooleanArgument("mustBeResolved"))
                .executesPlayer((player, args) -> {
                    Component component = (Component) args.getOrDefault("component", Component.text("Right"));
                    double width = (double) args.getOrDefault("width", DEFAULT_WIDTH);
                    boolean mustBeResolved = (boolean) args.getOrDefault("mustBeResolved", DEFAULT_MUST_BE_RESOLVED);

                    alignAndSend(player, component, Alignment.RIGHT, width, mustBeResolved);
                })
            )

            // align
            .withSubcommand(new CommandAPICommand("align")
                .withArguments(new MultiLiteralArgument("alignment", "left", "right", "center"))
                .withOptionalArguments(new ChatComponentArgument("component"))
                .withOptionalArguments(new DoubleArgument("width"))
                .withOptionalArguments(new BooleanArgument("mustBeResolved"))
                .executesPlayer((player, args) -> {
                    String alignment = (String) args.getOrDefault("alignment", "left");
                    Component component = (Component) args.getOrDefault("component", Component.text("Text"));
                    double width = (double) args.getOrDefault("width", DEFAULT_WIDTH);
                    boolean mustBeResolved = (boolean) args.getOrDefault("mustBeResolved", DEFAULT_MUST_BE_RESOLVED);

                    Alignment align = switch (alignment) {
                        case "left" -> Alignment.LEFT;
                        case "right" -> Alignment.RIGHT;
                        case "center" -> Alignment.CENTER;
                        default -> Alignment.LEFT;
                    };
                    alignAndSend(player, component, align, width, mustBeResolved);
                })
            )

            // measure
            .withSubcommand(new CommandAPICommand("measure")
                .withArguments(new ChatComponentArgument("component"))
                .withOptionalArguments(new BooleanArgument("mustBeResolved"))
                .executesPlayer((player, args) -> {
                    Component component = (Component) args.getOrDefault("component", Component.text("=== CALINEA TEST ==="));
                    boolean mustBeResolved = (boolean) args.getOrDefault("mustBeResolved", DEFAULT_MUST_BE_RESOLVED);

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

            // split
            .withSubcommand(new CommandAPICommand("split")
                .withArguments(new ChatComponentArgument("component"))
                .withOptionalArguments(new IntegerArgument("maxWidth"))
                .withOptionalArguments(new BooleanArgument("mustBeResolved"))
                .executesPlayer((player, args) -> {
                    Component component = (Component) args.get("component");
                    int maxWidth = (int) args.getOrDefault("maxWidth", DEFAULT_WIDTH);
                    boolean mustBeResolved = (boolean) args.getOrDefault("mustBeResolved", DEFAULT_MUST_BE_RESOLVED);

                    // Resolve
                    if (mustBeResolved) {
                        component = Calinea.resolve(component, player, player);
                    }

                    // Split
                    List<Component> splits = Calinea.split(component, maxWidth).components();

                    // Send header
                    player.sendMessage(Component.text()
                        .append(Component.text("Split into ", NamedTextColor.YELLOW))
                        .append(Component.text(splits.size(), NamedTextColor.YELLOW, TextDecoration.BOLD))
                        .append(Component.text(" parts (max width ", NamedTextColor.YELLOW))
                        .append(Component.text(maxWidth, NamedTextColor.YELLOW, TextDecoration.BOLD))
                        .append(Component.text("):", NamedTextColor.YELLOW))
                        .build());

                    // Send each part
                    for (var part : splits) {
                        player.sendMessage(part.hoverEvent(Component.text(part.toString())));
                    }
                })
            )

            // separator
            .withSubcommand(new CommandAPICommand("separator")
                .withOptionalArguments(new DoubleArgument("width"))
                .withOptionalArguments(new BooleanArgument("mustBeResolved"))
                .executesPlayer((player, args) -> {
                    double width = (double) args.getOrDefault("width", DEFAULT_WIDTH);
                    Component separator = Calinea.separator(Component.text("-", NamedTextColor.GRAY), width, true);
                    boolean mustBeResolved = (boolean) args.getOrDefault("mustBeResolved", DEFAULT_MUST_BE_RESOLVED);

                    // Resolve
                    if (mustBeResolved) {
                        separator = Calinea.resolve(separator, player, player);
                    }

                    player.sendMessage(separator);
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

    private static void alignAndSend(Entity entity, Component component, Alignment alignment, double width, boolean mustBeResolved) {
        Component result;
        LayoutBuilder layoutBuilder = Calinea.layout(component)
            .width(width);

        // Alignment
        switch (alignment) {
            case LEFT:
                layoutBuilder = layoutBuilder.align(Alignment.LEFT);
                break;
            case RIGHT:
                layoutBuilder = layoutBuilder.align(Alignment.RIGHT);
                break;
            case CENTER:
                layoutBuilder = layoutBuilder.align(Alignment.CENTER);
                break;
        }

        // Resolve
        if (mustBeResolved) {
            layoutBuilder = layoutBuilder.resolve(entity);
        }

        // Build
        result = layoutBuilder.build();

        // Send header with hover showing the component's toString()
        entity.sendMessage(Component.text()
            .append(Component.text("Aligned '", NamedTextColor.YELLOW))
            .append(Component.text(alignment.name().toLowerCase(), NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(Component.text("' ", NamedTextColor.YELLOW))
            .append(Component.text(" (width ", NamedTextColor.YELLOW))
            .append(Component.text(width, NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(Component.text("):", NamedTextColor.YELLOW))
            .appendNewline()
            .append(result.hoverEvent(Component.text(result.toString()))) // Show the component and its content on hover
            .build());
    }
}
