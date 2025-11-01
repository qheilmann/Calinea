package io.calinea.generator.model;

import java.util.Map;
import java.util.TreeMap;

/**
 * Represents font information including character widths.
 */
public class FontInfo {
    private final String name;
    private final Map<Integer, Integer> widths; // codepoint -> width
    private final int defaultWidth;
    
    public FontInfo(String name, int defaultWidth) {
        this.name = name;
        this.defaultWidth = defaultWidth;
        this.widths = new TreeMap<>();
    }
    
    public String getName() {
        return name;
    }
    
    public int getDefaultWidth() {
        return defaultWidth;
    }
    
    public Map<Integer, Integer> getWidths() {
        return widths;
    }
    
    public void setWidth(int codepoint, int width) {
        if (width != defaultWidth) {
            widths.put(codepoint, width);
        } else {
            widths.remove(codepoint); // Remove if same as default to save space
        }
    }
    
    public int getWidth(int codepoint) {
        return widths.getOrDefault(codepoint, defaultWidth);
    }
    
    /**
     * Returns only non-default widths for efficient storage.
     */
    public Map<Integer, Integer> getNonDefaultWidths() {
        return new TreeMap<>(widths);
    }
    
    @Override
    public String toString() {
        return String.format("FontInfo{name='%s', defaultWidth=%d, overrides=%d}", 
                           name, defaultWidth, widths.size());
    }
}