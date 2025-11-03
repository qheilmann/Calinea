package io.calinea.models;

import java.util.Map;
import java.util.TreeMap;

import net.kyori.adventure.key.Key;

/**
 * Represents font information including character widths.
 */
public class FontInfo {
    private final Key fontKey;
    private final Map<Integer, Integer> widths; // codepoint -> width
    
    public FontInfo(Key fontKey) {
        this.fontKey = fontKey;
        this.widths = new TreeMap<>();
    }
    
    public Key getFontKey() {
        return fontKey;
    }
    
    public Map<Integer, Integer> getWidths() {
        return widths;
    }
    
    public void setWidth(int codepoint, int width) {
        widths.put(codepoint, width);
    }
    
    /**
     * Gets the width for a specific codepoint.
     * @return -1 if no specific width is set.
     */
    public int getWidth(int codepoint) {
        return widths.getOrDefault(codepoint, -1);
    }
    
    @Override
    public String toString() {
        return String.format("FontInfo{name='%s', width=%d}", 
                           fontKey, widths.size());
    }
}