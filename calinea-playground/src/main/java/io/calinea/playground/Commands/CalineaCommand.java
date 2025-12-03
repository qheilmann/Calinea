package io.calinea.playground.Commands;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

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
import net.kyori.adventure.text.format.Style;
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
                                    /calinea example [exampleName] [width]
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
                    Component result = Component.text().style(Style.style(NamedTextColor.YELLOW))
                        .append(Component.text("Width of '"))
                        .append(component.applyFallbackStyle(NamedTextColor.WHITE).hoverEvent(Component.text(component.toString()))) // Show the component and its content on hover
                        .append(Component.text("': "))
                        .append(Component.text(String.valueOf(width), Style.style(TextDecoration.BOLD)))
                        .append(Component.text(" pixels"))
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
                    player.sendMessage(Component.text().style(Style.style(NamedTextColor.YELLOW))
                        .append(Component.text("Split into "))
                        .append(Component.text(String.valueOf(splits.size()), Style.style(TextDecoration.BOLD)))
                        .append(Component.text(" parts (max width: "))
                        .append(Component.text(String.valueOf(maxWidth), Style.style(TextDecoration.BOLD)))
                        .append(Component.text("):"))
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

                    player.sendMessage(Component.text().style(Style.style(NamedTextColor.YELLOW))
                        .append(Component.text("Separator '"))
                        .append(separator.hoverEvent(Component.text(separator.toString()))) // Show the component and its content on hover
                        .append(Component.text("' (width: "))
                        .append(Component.text(String.valueOf(width), Style.style(TextDecoration.BOLD)))
                        .append(Component.text("):"))
                        .build());
                    player.sendMessage(separator);
                })
            )

            // example
            .withSubcommand(new CommandAPICommand("example")
                .withArguments(new MultiLiteralArgument("exampleName", "AliceLetter", "SimpleCenter"))
                .withOptionalArguments(new DoubleArgument("width"))
                .executesPlayer((player, args) -> {
                    String exampleName = (String) args.getOrDefault("exampleName", "AliceLetter");
                    double width = (double) args.getOrDefault("width", 300.0);

                    player.sendMessage(Component.text().style(Style.style(NamedTextColor.YELLOW))
                        .append(Component.text("Example '"))
                        .append(Component.text(exampleName, Style.style(TextDecoration.BOLD)))
                        .append(Component.text(" (width: "))
                        .append(Component.text(String.valueOf(width), Style.style(TextDecoration.BOLD)))
                        .append(Component.text("):"))
                        .build());

                    switch (exampleName) {
                        case "AliceLetter":
                            exampleAliceLetter(player, width);
                            break;
                        case "SimpleCenter":
                            exampleSimpleCenter(player, width);
                            break;
                    
                        default:
                            player.sendMessage(Component.text("Unknown example: " + exampleName, NamedTextColor.RED));
                            break;
                    }
                })
            )

            // reload
            .withSubcommand(new CommandAPICommand("reload")
                .executes((sender, args) -> {

                    sender.sendMessage("Reloading calinea configuration ...");

                    CompletableFuture.runAsync(() -> {
                        Calinea.reloadPackInfo();
                    })
                    .thenAccept(aVoid -> {
                        sender.sendMessage(Component.text("Calinea configuration reloaded", NamedTextColor.GREEN));
                    })
                    .exceptionally(ex -> {
                        Calinea.logger().severe("Failed to reload calinea configuration", ex);
                        return null;
                    });
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
        entity.sendMessage(Component.text().style(Style.style(NamedTextColor.YELLOW))
            .append(Component.text("Aligned '"))
            .append(Component.text(alignment.name().toLowerCase(), Style.style(TextDecoration.BOLD)))
            .append(Component.text("' (width: "))
            .append(Component.text(String.valueOf(width), Style.style(TextDecoration.BOLD)))
            .append(Component.text("):"))
            .appendNewline()
            .append(result.hoverEvent(Component.text(result.toString()))) // Show the component and its content on hover
            .build());
    }

    
    private static void exampleAliceLetter(Player player, double width) {
        if (width <= 20) {
            player.sendMessage(Component.text("Width too small for Alice's letter example because there is a 20px right offset on the signature.", NamedTextColor.RED));
            return;
        }

        Component title = Component.text("Lettre de l'astronaute mystère", Style.style(TextDecoration.BOLD, TextDecoration.UNDERLINED));
        Component date = Component.text("Fait à Paris, le 14 juillet 2023");
        Component intro = Component.text("Cher ").append(Component.selector("@s")).appendNewline().appendNewline();
        Component signature = Component.text("-", NamedTextColor.DARK_GRAY).append(Component.text("XXXXX", Style.style(TextDecoration.OBFUSCATED)));
        String text = "Je t'écris de mon vaisseau spatial, en orbite autour de la Terre. Tout va bien ici. " +
            "La vue est magnifique, et je pense souvent à toi. J'espère que tout se passe bien pour toi là-bas. " +
            "Prends soin de toi et écris-moi vite !\n\nJe t'embrasse fort,";

        Component body = intro.append(Component.text(text, NamedTextColor.LIGHT_PURPLE));
        Component allignedBody = Calinea.layout(body)
            .width(width)
            .align(Alignment.LEFT)
            .resolve(player)
            .build();

        Component letter = Component.text()
            .append(Calinea.center(title, width)).appendNewline().appendNewline() // Center title
            .append(Calinea.alignRight(date, width)).appendNewline()              // Right-align date
            .append(allignedBody).appendNewline()                                 // Automatic line breaks
            .append(Calinea.alignRight(signature, width, 20))       // Right-align signature with a -20 pixel offset
            .build();

        player.sendMessage(letter);
    }

    private static void exampleSimpleCenter(Player player, double width) {
        Component complexComponent = Component.text()
            .append(Component.text("=== ", NamedTextColor.DARK_AQUA))
            .append(Component.text("Calinea ", NamedTextColor.AQUA, TextDecoration.BOLD))
            .append(Component.text("Playground ", NamedTextColor.DARK_AQUA))
            .append(Component.text("===\n", NamedTextColor.DARK_AQUA))
            .append(Component.text("This is an ", NamedTextColor.WHITE))
            .append(Component.text("example ", NamedTextColor.GOLD, TextDecoration.ITALIC))
            .append(Component.text("of a ", NamedTextColor.WHITE))
            .append(Component.text("complex ", NamedTextColor.LIGHT_PURPLE, TextDecoration.UNDERLINED))
            .append(Component.text("component.", NamedTextColor.WHITE))
            .build();

        Component centered = Calinea.layout(complexComponent)
            .width(width)
            .align(Alignment.CENTER)
            .resolve(player)
            .build();

        player.sendMessage(centered);
    }
}
