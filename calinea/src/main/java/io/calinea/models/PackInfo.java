package io.calinea.models;

import java.util.Collections;
import java.util.Map;

import org.jetbrains.annotations.Unmodifiable;

import net.kyori.adventure.key.Key;

public class PackInfo {
    private static final int DEFAULT_CHAR_WIDTH = 6;
    private Map<Key, FontInfo> fonts;

    public PackInfo() {
        this.fonts = Collections.emptyMap();
    }

    public PackInfo(Map<Key, FontInfo> fonts) {
        this.fonts = fonts;
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

    public int getWidth(Key fontKey, int codepoint) {
        FontInfo fontInfo = fonts.get(fontKey);
        if (fontInfo != null) {
            return fontInfo.getWidth(codepoint);
        }
        
        return DEFAULT_CHAR_WIDTH;
    }
}
