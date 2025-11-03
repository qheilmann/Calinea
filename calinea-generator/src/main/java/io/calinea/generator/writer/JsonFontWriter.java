package io.calinea.generator.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.calinea.models.FontInfo;
import io.calinea.reader.JsonFontFormat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Writes font width data in JSON format for easier debugging and manual editing.
 * Creates a single file containing all fonts with their character width mappings.
 */
public class JsonFontWriter {
    private final ObjectMapper objectMapper;
    
    public JsonFontWriter() {
        @SuppressWarnings("null")
        JsonFactory factory = JsonFactory.builder()
            .configure(JsonWriteFeature.ESCAPE_NON_ASCII, true)
            .build();
        this.objectMapper = new ObjectMapper(factory);
    }
    
    /**
     * Writes all fonts to a single JSON file.
     */
    public void writeFonts(List<FontInfo> fonts, Path outputFile) throws IOException {
        // Ensure parent directory exists
        Files.createDirectories(outputFile.getParent());
        
        ObjectNode root = objectMapper.createObjectNode();
        
        // Metadata
        root.put("version", JsonFontFormat.CURRENT_VERSION);
        root.put("format", JsonFontFormat.FORMAT);
        root.put("description", "Character width mappings for Minecraft fonts. Use https://r12a.github.io/app-conversion/ (JS/Java/C category, ES6 disabled) to convert between \\u format and visual representation.");
        
        // Fonts array
        ArrayNode fontsArray = root.putArray("fonts");
        
        for (FontInfo font : fonts) {
            ObjectNode fontNode = fontsArray.addObject();
            fontNode.put("fontKey", font.getFontKey().asString());
            fontNode.put("default_width", font.getDefaultWidth());
            
            // Character widths (only non-default values)
            ObjectNode widthsNode = fontNode.putObject("widths");
            Map<Integer, Integer> nonDefaultWidths = font.getNonDefaultWidths();
            
            for (Map.Entry<Integer, Integer> entry : nonDefaultWidths.entrySet()) {
                int codepoint = entry.getKey();
                int width = entry.getValue();
                
                // Use actual character - Jackson will escape non-ASCII as Unicode
                String key = new String(Character.toChars(codepoint));
                
                widthsNode.put(key, width);
            }
        }
        
        // Write with custom pretty formatting for better array indentation
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        objectMapper.writer(printer).writeValue(outputFile.toFile(), root);
        
        System.out.println("Generated JSON font file: " + outputFile);
        System.out.println("  - " + fonts.size() + " fonts");
        
        // Print detailed statistics for each font
        for (FontInfo font : fonts) {
            Map<Integer, Integer> nonDefaultWidths = font.getNonDefaultWidths();
            if (!nonDefaultWidths.isEmpty()) {
                int minCodepoint = nonDefaultWidths.keySet().stream().mapToInt(Integer::intValue).min().orElse(0);
                int maxCodepoint = nonDefaultWidths.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
                System.out.println("    Font '" + font.getFontKey() + "': " + nonDefaultWidths.size() + 
                                 " overrides (range: U+" + String.format("%04X", minCodepoint) + 
                                 " to U+" + String.format("%04X", maxCodepoint) + ")");
            } else {
                System.out.println("    Font '" + font.getFontKey() + "': no character width overrides");
            }
        }
    }
}