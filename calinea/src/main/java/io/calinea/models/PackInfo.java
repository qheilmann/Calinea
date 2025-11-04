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
    private static final int DEFAULT_CHAR_WIDTH = 5;

    private Map<Key, FontInfo> fonts;
    private final int defaultWidth;

    public PackInfo() {
        this(List.of(), DEFAULT_CHAR_WIDTH);
    }

    public PackInfo(SequencedCollection<FontInfo> fonts) {
        this(fonts, DEFAULT_CHAR_WIDTH);
    }

    public PackInfo(int defaultWidth) {
        this(List.of(), defaultWidth);
    }

    public PackInfo(SequencedCollection<FontInfo> fonts, int defaultWidth) {
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

    public int getDefaultWidth() {
        return defaultWidth;
    }

    /**
     * Gets the width of a character in a specific font.
     * If the font or character is not found, returns the default width.
     * Resolves references recursively.
     */
    public int getWidth(Key fontKey, int codepoint) {
        int width = getWidth(fontKey, codepoint, new HashSet<>());
        if (width == -1) {
            if(Calinea.getConfig().warnOnMissingWidths()){
                Calinea.getLogger().warning("Character '" + Character.toString(codepoint) + "' not found in font '" + fontKey.asString() + "' or its references. Using default width '" + defaultWidth + "'.");
            }
            return defaultWidth;
        }
        return width;
    }
    
    /**
     * Internal method to get width with circular reference protection.
     * @return The width if found, or -1 if not found (caller should use defaultWidth)
     */
    private int getWidth(Key fontKey, int codepoint, Set<Key> visited) {
        FontInfo fontInfo = fonts.get(fontKey);

        if (fontInfo == null) {
            if(Calinea.getConfig().warnOnMissingFonts()){
                Calinea.getLogger().warning("Font with key '" + fontKey.asString() + "' not found.");
            }
            return -1; // Font not found
        }
        
        // Check for circular references
        if (visited.contains(fontKey)) {
            if(Calinea.getConfig().verboseLogging()){
                Calinea.getLogger().info("Circular reference detected for font '" + fontKey.asString() + "', stopping recursion.");
            }
            return -1; // Circular reference
        }
        
        visited.add(fontKey);

        try {
            // Check direct width first
            int width = fontInfo.getDirectWidth(codepoint);
            if (width != -1) {
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
                if (width != -1) {
                    return width; // Found in reference
                }
            }
        } finally {
            visited.remove(fontKey);
        }

        // Not found anywhere in this font or its references
        return -1;
    }
}
