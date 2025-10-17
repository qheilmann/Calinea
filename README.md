# Calinea

A Java library for measuring and aligning Adventure components in Minecraft plugins.

## Installation

**Gradle:**
```kotlin
implementation("io.calinea:calinea:0.3.0-SNAPSHOT")
```

**Maven:**
```xml
<dependency>
    <groupId>io.calinea</groupId>
    <artifactId>calinea</artifactId>
    <version>0.3.0-SNAPSHOT</version>
</dependency>
```

## Usage

```java
import io.calinea.Calinea;

// Center text
Component centered = Calinea.center("Hello World");

// Align text
Component left = Calinea.alignLeft("Left", 200);
Component right = Calinea.alignRight("Right", 200);

// Measure width
int width = Calinea.measureWidth("Text");
```

## Building

```bash
./gradlew build
```

## Modules

- **calinea** - Core library
- **calinea-generator** - Font atlas generator
- **calinea-playground** - Example plugin

## License

MIT - see [`LICENSE.txt`](LICENSE.txt)