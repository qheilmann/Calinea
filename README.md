# Calinea

[![](https://jitpack.io/v/qheilmann/Calinea.svg)](https://jitpack.io/#qheilmann/Calinea)

A server-side Java library designed for modifying [Adventure](https://docs.advntr.dev/) components, enabling alignment, precise measurement and layout management. Whether you need to center text in a GUI, split long messages, or create complex layouts with most component types, styles, fonts or custom characters, Calinea provides the tools to ensure pixel-perfect rendering.

## Features

- **Text Alignment**: Easily align Adventure Components to the **Left**, **Right**, or **Center**.
- **Precise Measurement**: Accurately calculate **width** and **split** components.
- **Multi-line Support**: Automatically handles multi-line components and respects line breaks.
- **Layout Builder**: Fluent API for complex layouts with **resolution**, **padding** and **filling**.
- **Font Support**: Handles **all Minecraft characters**, **custom bitmap/space characters**, and **multiple fonts**.
- **Rich Styling**: Preserves component styles (colors, bold, italic, etc.) even hierarchically.
- **High Precision**: Uses very precise spacing for near-perfect alignments, even with non-integer widths.
- **Broad Compatibility**: Supports almost all Adventure components (see [Limitations](#Client-side-resolved-components)).

## Installation

### Gradle
```kotlin
implementation 'com.github.qheilmann:Calinea:v0.5.0'
```

### Maven
```xml
<dependency>
    <groupId>com.github.qheilmann</groupId>
    <artifactId>Calinea</artifactId>
    <version>v0.5.0</version>
</dependency>
```

## Resource Pack
Your server requires a resource pack with the font used for Calinea measurements to ensure all players use consistent fonts. This resource pack must include the `minecraft:default` font to prevent players from overriding it with their own resource packs, which could break measurements. Alternatively, you can use a custom font exclusively for alignments.

To enable accurate text measurement, provide a JSON file containing character widths for your font. See [Calinea Generator](./calinea-generator/README.md) for details on generating this file.

## API Usage

### Initialize Calinea
Can be used inside `JavaPlugin#onLoad()`
```java
// Get the path to the calinea config JSON file
Path calineaConfigPath = getDataFolder().toPath().resolve("calinea-config.json");

// Create Calinea configuration
CalineaConfig config = new CalineaConfig()
    .calineaConfigPath(calineaConfigPath)
    .logger(CalineaLogger.fromSlf4jLogger(LOGGER));

// Load Calinea with the configuration
Calinea.onLoad(config);
```

### Basic alignments

Simple text alignments can be done using static methods:

```java
Component myText = Component.text("Hello"); // "Hello"

Component LeftAlignedText = Calinea.alignLeft(myText, 200);   // "Hello          "
Component CenteredText = Calinea.center(myText, 200);         // "     Hello     "
Component RightAlignedText = Calinea.alignRight(myText, 200); // "          Hello"
```

> [!WARNING]  
> Components here aren't resolved, so any component that needs server-side resolution (like selectors, nbt, etc.) will not display correctly. You should resolve them with `Calinea#resolve` before or use the Layout Builder below.

### Layout Builder

Alignment can also be done using the fluent builder:

```java
Component layout = Calinea.layout(Component.text("Complex Layout"))
    .width(320)                 // Set the target width
    .align(Alignment.CENTER)    // Center alignment
    .resolve(player, subject)   // Resolve all components for the given player and subject
    .padding(50, 0)             // Add 50px padding on left and 0px on right
    .fillLines(true)            // Completely fill lines to the width
    .build();
```

You can also set custom text resolver, measurer, splitter, etc.

### Other Features

```java
// Resolve component
Component resolved = Calinea.resolve(Component.selector("@s"), player);

// Measure width
double width = Calinea.measure(Component.text("How wide am I?"));

// Split text
List<Component> lines = Calinea.split(Component.text("This is a long text that needs to be split."), 100);

// Separator
Component separator = Calinea.separator(Component.text("-"), 100, true);
```

#### A complex example

```java
Component title = Component.text("Lettre de l'astronaute mystère", Style.style(TextDecoration.BOLD, TextDecoration.UNDERLINED));
Component date = Component.text("Fait depuis l'espace, le 25 novembre 2025");
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
    .append(Calinea.alignRight(signature, width, 20))                     // Right-align signature with a -20 pixel offset
    .build();

//             Lettre de l'astronaute mystère                 
//
//                   Fait depuis l'espace, le 25 novembre 2025
// Cher Alice
//
// Je t'écris de mon vaisseau spatial, en orbite autour de la
// Terre. Tout va bien ici. La vue est magnifique, et je pense 
// souvent à toi. J'espère que tout se passe bien pour toi là-
// bas. Prends soin de toi et écris-moi vite !
//
// Je t'embrasse fort,
//                                              -XXXXX
```

### Limitations

#### Minecraft chat

If you use this in Minecraft chat, be aware that the chat width can vary depending on player settings. This library is more suited for defined widths such as dialog.

#### Client side resolved components

Some components that rely on client-side resolution (like translations or keybinds) can't be correctly measured.  
The only current workaround is to force them on server-side before measuring. But this completely defeats the purpose of using them instead of classic text components.

#### Platform support

Currently, only Paper is supported. Other platforms may work but are not officially supported.

## Modules

- **calinea** - Core library
- **calinea-generator** - Font atlas generator [Calinea Generator](./calinea-generator/README.md)
- **calinea-playground** - Example plugin [Calinea Playground](./calinea-playground/README.md)

## License

MIT - see [`LICENSE.txt`](LICENSE.txt)