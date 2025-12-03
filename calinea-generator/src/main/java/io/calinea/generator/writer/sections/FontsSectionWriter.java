package io.calinea.generator.writer.sections;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.calinea.generator.writer.JsonNodeHelper;
import io.calinea.pack.font.FontInfo;
import io.calinea.pack.font.FontsInfo;
import net.kyori.adventure.key.Key;

import java.util.Map;

/**
 * Writes the fonts section containing character width mappings for each font.
 */
public class FontsSectionWriter implements ISectionWriter {
    
    private final FontsInfo fontsInfo;
    
    public FontsSectionWriter(FontsInfo fontsInfo) {
        this.fontsInfo = fontsInfo;
    }
    
    @Override
    public String getSectionName() {
        return "fonts";
    }
    
    @Override
    public boolean hasData() {
        return !fontsInfo.getFonts().isEmpty();
    }
    
    @Override
    public void writeSection(ObjectNode root) {
        ObjectNode fontsNode = root.putObject("fonts");
        
        // Pack default width
        JsonNodeHelper.putNumber(fontsNode, "default_width", fontsInfo.getDefaultWidth());

        // Fonts array
        ArrayNode fontsArray = fontsNode.putArray("entries");
        
        for (FontInfo font : fontsInfo.getFonts().values()) {
            writeFontEntry(fontsArray, font);
        }
    }
    
    private void writeFontEntry(ArrayNode fontsArray, FontInfo font) {
        ObjectNode fontNode = fontsArray.addObject();
        fontNode.put("fontKey", font.getFontKey().asString());
        
        // Add references if any
        if (font.hasReferences()) {
            ArrayNode referencesArray = fontNode.putArray("references");
            for (Key referenceKey : font.getReferences()) {
                referencesArray.add(referenceKey.asString());
            }
        }
        
        // Character widths (only if not empty)
        Map<Integer, Double> widths = font.getWidths();
        if (!widths.isEmpty()) {
            writeWidths(fontNode, widths);
        }
    }
    
    private void writeWidths(ObjectNode fontNode, Map<Integer, Double> widths) {
        ObjectNode widthsNode = fontNode.putObject("widths");
        
        for (Map.Entry<Integer, Double> entry : widths.entrySet()) {
            int codepoint = entry.getKey();
            double width = entry.getValue();
            
            // Use actual character - Jackson will escape non-ASCII as Unicode
            String key = new String(Character.toChars(codepoint));
            
            // Add width value using optimal number representation
            JsonNodeHelper.putNumber(widthsNode, key, width);
        }
    }
    
    @Override
    public void printStatistics() {
        System.out.println("  - " + fontsInfo.getFonts().size() + " fonts");
        System.out.println("  - Pack default width: " + fontsInfo.getDefaultWidth());
        
        // Print detailed statistics for each font
        for (FontInfo font : fontsInfo.getFonts().values()) {
            printFontStatistics(font);
        }
    }
    
    private void printFontStatistics(FontInfo font) {
        Map<Integer, Double> widths = font.getWidths();
        
        if (font.hasReferences()) {
            String referencesStr = font.getReferences().stream()
                .map(Key::asString)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
            
            if (!widths.isEmpty()) {
                System.out.println("    Font '" + font.getFontKey() + "': references [" + referencesStr + "] + " + widths.size() + " overrides");
            } else {
                System.out.println("    Font '" + font.getFontKey() + "': references [" + referencesStr + "]");
            }
        } else if (!widths.isEmpty()) {
            int minCodepoint = widths.keySet().stream().mapToInt(Integer::intValue).min().orElse(0);
            int maxCodepoint = widths.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
            System.out.println("    Font '" + font.getFontKey() + "': " + widths.size() + 
                             " widths (range: U+" + String.format("%04X", minCodepoint) + 
                             " to U+" + String.format("%04X", maxCodepoint) + ")");
        } else {
            System.out.println("    Font '" + font.getFontKey() + "': no character width overrides");
        }
    }
}
