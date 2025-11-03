package io.calinea.models;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;

import org.jetbrains.annotations.Unmodifiable;

import net.kyori.adventure.key.Key;

public class PackInfo {
    private static final int DEFAULT_CHAR_WIDTH = 6;

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
     */
    public int getWidth(Key fontKey, int codepoint) {
        FontInfo fontInfo = fonts.get(fontKey);
        if (fontInfo != null) {
            int width = fontInfo.getWidth(codepoint);
            if (width != -1) {
                return width;
            }
            // TODO depending of the config (if warn, warn missing char width, if silent juste return default)
        }
        
        return defaultWidth;
    }
}
