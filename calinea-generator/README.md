# Calinea Generator

A command-line tool for generating Calinea config files from Minecraft resource packs. It analyzes fonts and translations in your resource pack and outputs a JSON file containing character width mappings required by the Calinea library.

## Generator Jar

### Release Downloads

You can download the latest released JAR from the [Releases](https://github.com/qheilmann/Calinea/releases).

### Building

You can also build the generator from source. The generator requires Java 21 or higher. Build the executable JAR using Gradle:

```bash
./gradlew :calinea-generator:shadowJar
```

This creates a standalone JAR at:
```
calinea-generator/build/libs/calinea-generator-<version>.jar
```

## Usage

```bash
java -jar calinea-generator.jar <resource-pack-path> [output-path]
```

### Arguments

| Argument | Description |
|----------|-------------|
| `<resource-pack-path>` | Path to your Minecraft resource pack folder |
| `[output-path]` | Output directory or file path (defaults to `./calinea-output/`) |

#### Examples
```bash
java -jar calinea-generator.jar ./my-resource-pack # Creates: ./calinea-output/calinea-config.json
java -jar calinea-generator.jar ./my-resource-pack ./output # Creates: ./output/calinea-config.json
java -jar calinea-generator.jar ./my-resource-pack ./output/custom-name.json # Creates: ./output/custom-name.json
```

## Output

The generator produces a JSON file containing:

- **Font widths**: Character width mappings for all fonts found in the resource pack
- **Translations**: Translation keys and their values for server-side resolution

This file should be placed in your server folder (like your plugin's data folder) and loaded using `CalineaConfig#calineaConfigPath()` during Calinea initialization.

## Resource Pack Requirements

Your resource pack should follow the standard Minecraft resource pack structure:

```
my-resource-pack/
├── pack.mcmeta
└── assets/
    ├── minecraft/
    │   ├── font/
    │   │   └── *.json
    │   ├── textures/
    │   │   └── font/
    │   └── lang/
    │       └── *.json
    └── <namespace>/
        └── ...
```

The generator will parse:
- All font definition files (`assets/*/font/*.json`), and their corresponding Bitmap font textures for character width calculation
- Translation files (`assets/*/lang/*.json`) for server-side translation support

