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
    private final Map<Integer, Double> widths; // codepoint -> width
    private final List<Key> references; // fonts this font references
    
    public FontInfo(Key fontKey) {
        this.fontKey = fontKey;
        this.widths = new TreeMap<>();
        this.references = new ArrayList<>();
    }
    
    public Key getFontKey() {
        return fontKey;
    }
    
    public Map<Integer, Double> getWidths() {
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
    
    public void setWidth(int codepoint, double width) {
        widths.put(codepoint, width);
    }
    
    /**
     * Gets the direct width for a specific codepoint from this font only.
     * Does not resolve references, use {@link PackInfo#getWidth(Key, int)} for that.
     * @return WidthResult containing the width if found, or MISSING_WIDTH status if not set.
     */
    public WidthResult getDirectWidth(int codepoint) {
        Double width = widths.get(codepoint);
        if (width != null) {
            return WidthResult.found(width);
        }
        return WidthResult.missingWidth();
    }


    
    @Override
    public String toString() {
        if (hasReferences()) {
            return String.format("FontInfo{key='%s', nbOfDirectCharacters=%d, references=%s}", 
                               fontKey, widths.size(), references);
        } else {
            return String.format("FontInfo{key='%s', nbOfDirectCharacters=%d}", 
                               fontKey, widths.size());
        }
    }
}