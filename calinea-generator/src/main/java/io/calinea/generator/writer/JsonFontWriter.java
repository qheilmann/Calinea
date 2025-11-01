package io.calinea.generator.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import io.calinea.generator.model.FontInfo;

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
        root.put("version", "1.0");
        root.put("format", "calinea-font-widths");
        root.put("description", "Character width mappings for Minecraft fonts. Use https://r12a.github.io/app-conversion/ (JS/Java/C category, ES6 disabled) to convert between \\u format and visual representation.");
        
        // Fonts array
        ArrayNode fontsArray = root.putArray("fonts");
        
        for (FontInfo font : fonts) {
            ObjectNode fontNode = fontsArray.addObject();
            fontNode.put("name", font.getName());
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
                System.out.println("    Font '" + font.getName() + "': " + nonDefaultWidths.size() + 
                                 " overrides (range: U+" + String.format("%04X", minCodepoint) + 
                                 " to U+" + String.format("%04X", maxCodepoint) + ")");
            } else {
                System.out.println("    Font '" + font.getName() + "': no character width overrides");
            }
        }
    }
    
    /**
     * Reads fonts from a JSON file.
     */
    public List<FontInfo> readFonts(Path jsonFile) throws IOException {
        JsonNode root = objectMapper.readTree(jsonFile.toFile());
        
        // Verify format
        String format = root.path("format").asText("");
        if (!format.equals("calinea-font-widths")) {
            throw new IOException("Invalid format: expected 'calinea-font-widths', got '" + format + "'");
        }
        
        JsonNode fontsArray = root.get("fonts");
        if (fontsArray == null || !fontsArray.isArray()) {
            throw new IOException("Missing or invalid 'fonts' array in JSON file");
        }
        
        List<FontInfo> fonts = new java.util.ArrayList<>();
        
        for (JsonNode fontNode : fontsArray) {
            String name = fontNode.get("name").asText();
            int defaultWidth = fontNode.get("default_width").asInt(6);
            
            FontInfo fontInfo = new FontInfo(name, defaultWidth);
            
            JsonNode widthsNode = fontNode.get("widths");
            if (widthsNode != null && widthsNode.isObject()) {
                widthsNode.fields().forEachRemaining(entry -> {
                    String key = entry.getKey();
                    int width = entry.getValue().asInt();
                    
                    // Parse codepoint from key
                    int codepoint = parseCodepointFromKey(key);
                    if (codepoint >= 0) {
                        fontInfo.setWidth(codepoint, width);
                    }
                });
            }
            
            fonts.add(fontInfo);
        }
        
        return fonts;
    }
    
    /**
     * Parses a codepoint from various key formats.
     */
    private int parseCodepointFromKey(String key) {
        try {
            // Try direct numeric parsing first
            return Integer.parseInt(key);
        } catch (NumberFormatException e) {
            // Handle Unicode escape format: "\u0030" or "\uD83D\uDE00" (surrogate pair)
            if (key.startsWith("\\u")) {
                if (key.length() == 6) {
                    // Single BMP character: \u0030
                    return Integer.parseInt(key.substring(2), 16);
                } else if (key.length() == 12 && key.substring(6, 8).equals("\\u")) {
                    // Surrogate pair: \uD83D\uDE00
                    int highSurrogate = Integer.parseInt(key.substring(2, 6), 16);
                    int lowSurrogate = Integer.parseInt(key.substring(8, 12), 16);
                    return Character.toCodePoint((char) highSurrogate, (char) lowSurrogate);
                }
            }
            
            // Legacy support: Try extracting from parentheses: "'A' (65)" -> 65
            int openParen = key.indexOf('(');
            int closeParen = key.indexOf(')', openParen);
            if (openParen >= 0 && closeParen > openParen) {
                String codepointStr = key.substring(openParen + 1, closeParen);
                try {
                    return Integer.parseInt(codepointStr);
                } catch (NumberFormatException e2) {
                    // Might be Unicode format: "U+00A0"
                    if (codepointStr.startsWith("U+")) {
                        return Integer.parseInt(codepointStr.substring(2), 16);
                    }
                }
            }
            
            // Try Unicode format at start: "U+00A0"
            if (key.startsWith("U+")) {
                String hex = key.substring(2);
                return Integer.parseInt(hex, 16);
            }
            
            // Single character format: "'A'"
            if (key.startsWith("'") && key.length() >= 3 && key.charAt(2) == '\'') {
                return key.charAt(1);
            }
            
            System.err.println("Could not parse codepoint from key: " + key);
            return -1;
        }
    }
}