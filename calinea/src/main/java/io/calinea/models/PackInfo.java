package io.calinea.models;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.Set;

import org.jetbrains.annotations.Unmodifiable;

import io.calinea.Calinea;
import net.kyori.adventure.key.Key;

public class PackInfo {
    public static final double MISSING_WIDTH = FontInfo.MISSING_WIDTH;
    public static final double MISSING_FONT = -2.0;
    public static final double CIRCULAR_REFERENCE = -3.0;

    public static final double DEFAULT_CHAR_WIDTH = 5.0;

    private Map<Key, FontInfo> fonts;
    private final double defaultWidth;

    public PackInfo() {
        this(List.of(), DEFAULT_CHAR_WIDTH);
    }

    public PackInfo(SequencedCollection<FontInfo> fonts) {
        this(fonts, DEFAULT_CHAR_WIDTH);
    }

    public PackInfo(double defaultWidth) {
        this(List.of(), defaultWidth);
    }

    public PackInfo(SequencedCollection<FontInfo> fonts, double defaultWidth) {
        this.defaultWidth = defaultWidth;
        this.fonts = new LinkedHashMap<>();

        for (FontInfo font : fonts) {
            this.fonts.put(font.getFontKey(), font);
        }
    }

    public PackInfo addFont(FontInfo fontInfo) {
        this.fonts.put(fontInfo.getFontKey(), fontInfo);
        return this;
    }

    public FontInfo getFont(Key key) {
        return fonts.get(key);
    }

    @Unmodifiable
    public Map<Key, FontInfo> getFonts() {
        return Collections.unmodifiableMap(fonts);
    }

    public double getDefaultWidth() {
        return defaultWidth;
    }

    /**
     * Gets the width of a character in a specific font.
     * If the font or character is not found, returns the default width.
     * Resolves references recursively.
     */
    public double getWidth(Key fontKey, int codepoint) {
        double width = getWidth(fontKey, codepoint, new HashSet<>());

        // Missing
        if (width <= 0) {
            if (width == MISSING_WIDTH && Calinea.getConfig().warnOnMissingWidths()){
                Calinea.getLogger().warning("Character '" + Character.toString(codepoint) + "' not found in font '" + fontKey.asString() + "' or its references. Using default width '" + defaultWidth + "'.");
            }

            if (width == MISSING_FONT && Calinea.getConfig().warnOnMissingFonts()){
                Calinea.getLogger().warning("Font with key '" + fontKey.asString() + "' not found. Using default width '" + defaultWidth + "'.");
            }

            return defaultWidth;
        }

        return width;
    }
    
    /**
     * Internal method to get width with circular reference protection.
     * @return The width if found, or -1 if character not found, or {@link PackInfo#MISSING_FONT} if font not found, or -3 if circular reference detected.
     */
    private double getWidth(Key fontKey, int codepoint, Set<Key> visited) {
        FontInfo fontInfo = fonts.get(fontKey);

        if (fontInfo == null) {
            return MISSING_FONT;
        }
        
        // Check for circular references
        if (visited.contains(fontKey)) {
            if(Calinea.getConfig().verboseLogging()){
                Calinea.getLogger().info("Circular reference detected for font '" + fontKey.asString() + "', stopping recursion.");
            }
            return CIRCULAR_REFERENCE; // Circular reference
        }
        
        visited.add(fontKey);

        try {
            // Check direct width first
            double width = fontInfo.getDirectWidth(codepoint);
            if (width >= 0) {
                return width; // Found directly
            }
            
            // Check references in order
            for (Key referenceKey : fontInfo.getReferences()) {
                // Skip if this would create a circular reference
                if (visited.contains(referenceKey)) {
                    if(Calinea.getConfig().verboseLogging()){
                        Calinea.getLogger().info("Skipping circular reference from '" + fontKey.asString() + "' to '" + referenceKey.asString() + "'.");
                    }
                    continue; // Skip this reference, try the next one
                }
                
                width = getWidth(referenceKey, codepoint, visited);
                if (width >= 0) {
                    return width; // Found in reference
                }
            }
        } finally {
            visited.remove(fontKey);
        }

        // Not found anywhere in this font or its references
        return MISSING_WIDTH;
    }
}
