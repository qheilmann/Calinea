package io.calinea.pack.reader;

import com.fasterxml.jackson.databind.JsonNode;

import io.calinea.pack.font.FontInfo;
import io.calinea.pack.font.FontsInfo;

import net.kyori.adventure.key.Key;

import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Reads the "fonts" section from Calinea config JSON.
 */
public class FontsSectionReader implements ISectionReader<FontsInfo> {
    
    private static final String SECTION_NAME = "fonts";
    
    @Override
    public String getSectionName() {
        return SECTION_NAME;
    }
    
    @Override
    public FontsInfo read(JsonNode fontsNode) throws IOException {
        // Read default_width (required)
        JsonNode defaultWidthNode = fontsNode.get("default_width");
        if (defaultWidthNode == null || !defaultWidthNode.isNumber()) {
            throw new IOException("Missing or invalid 'default_width' in fonts section");
        }
        double defaultWidth = defaultWidthNode.asDouble();
        
        FontsInfo fontsInfo = new FontsInfo(defaultWidth);
        
        // Read entries array
        @Nullable JsonNode entriesArray = fontsNode.get("entries");
        if (entriesArray == null || !entriesArray.isArray()) {
            throw new IOException("Missing or invalid 'entries' array in fonts section");
        }
        
        for (JsonNode fontNode : entriesArray) {
            FontInfo fontInfo = readFontEntry(fontNode);
            fontsInfo.addFont(fontInfo);
        }
        
        return fontsInfo;
    }
    
    private FontInfo readFontEntry(JsonNode fontNode) throws IOException {
        // Read fontKey
        @Nullable JsonNode fontKeyNode = fontNode.get("fontKey");
        if (fontKeyNode == null || fontKeyNode.asText().isEmpty()) {
            throw new IOException("Missing or invalid 'fontKey' in font entry");
        }
        
        Key fontKey = Key.key(fontKeyNode.asText());
        FontInfo fontInfo = new FontInfo(fontKey);
        
        // Read references (optional)
        @Nullable JsonNode referencesNode = fontNode.get("references");
        if (referencesNode != null && referencesNode.isArray()) {
            for (JsonNode referenceNode : referencesNode) {
                Key referenceKey = Key.key(referenceNode.asText());
                fontInfo.addReference(referenceKey);
            }
        }
        
        // Read widths (optional)
        @Nullable JsonNode widthsNode = fontNode.get("widths");
        if (widthsNode != null && widthsNode.isObject()) {
            widthsNode.fields().forEachRemaining(entry -> {
                String charKey = entry.getKey();
                double width = entry.getValue().asDouble();
                int codepoint = charKey.codePointAt(0);
                fontInfo.setWidth(codepoint, width);
            });
        }
        
        return fontInfo;
    }
}
