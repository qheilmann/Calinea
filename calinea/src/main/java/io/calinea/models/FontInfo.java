package io.calinea.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.kyori.adventure.key.Key;

/**
 * Represents font information including character widths and references.
 */
public class FontInfo {
    private final Key fontKey;
    private final Map<Integer, Integer> widths; // codepoint -> width
    private final List<Key> references; // fonts this font references
    
    public FontInfo(Key fontKey) {
        this.fontKey = fontKey;
        this.widths = new TreeMap<>();
        this.references = new ArrayList<>();
    }
    
    public Key getFontKey() {
        return fontKey;
    }
    
    public Map<Integer, Integer> getWidths() {
        return widths;
    }
    
    public List<Key> getReferences() {
        return references;
    }
    
    public void addReference(Key fontKey) {
        if (!references.contains(fontKey)) {
            references.add(fontKey);
        }
    }
    
    public void setReferences(List<Key> references) {
        this.references.clear();
        this.references.addAll(references);
    }
    
    public boolean hasReferences() {
        return !references.isEmpty();
    }
    
    public void setWidth(int codepoint, int width) {
        widths.put(codepoint, width);
    }
    
    /**
     * Gets the direct width for a specific codepoint from this font only.
     * Does not resolve references, use {@link PackInfo#getCharacterWidth(int, FontInfo)} for that.
     * @return -1 if no specific width is set.
     */
    public int getDirectWidth(int codepoint) {
        return widths.getOrDefault(codepoint, -1);
    }
    
    @Override
    public String toString() {
        if (hasReferences()) {
            return String.format("FontInfo{key='%s', widths=%d, references=%s}", 
                               fontKey, widths.size(), references);
        } else {
            return String.format("FontInfo{key='%s', widths=%d}", 
                               fontKey, widths.size());
        }
    }
}