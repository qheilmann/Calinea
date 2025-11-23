package io.calinea.generator.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.calinea.font.FontInfo;
import io.calinea.font.PackInfo;
import io.calinea.font.reader.JsonFontFormat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import net.kyori.adventure.key.Key;

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
    
    private boolean isWholeNumber(double value) {
        // Check for NaN and infinite values
        if (!Double.isFinite(value)) {
            return false;
        }
        
        // Get the rounded value
        long roundedValue = Math.round(value);
        
        // Check if the rounded value is within the safe integer range
        if (roundedValue < Integer.MIN_VALUE || roundedValue > Integer.MAX_VALUE) {
            return false;
        }
        
        // Check if the value is close enough to the rounded value
        // Using epsilon for floating-point comparison safety
        double epsilon = 1e-10;
        return Math.abs(value - roundedValue) < epsilon;
    }
    
    /**
     * Adds a numeric value to a JSON ObjectNode, using integer representation
     * for whole numbers and double representation for fractional values.
     * 
     * @param node the ObjectNode to add the value to
     * @param key the key for the value
     * @param value the numeric value to add
     */
    private void jsonPutNumber(ObjectNode node, String key, double value) {
        if (isWholeNumber(value)) {
            node.put(key, (int) Math.round(value));
        } else {
            node.put(key, value);
        }
    }
    
    /**
     * Writes PackInfo to a single JSON file.
     */
    public void writePackInfo(PackInfo packInfo, Path outputFile) throws IOException {
        // Ensure parent directory exists
        Files.createDirectories(outputFile.getParent());
        
        ObjectNode root = objectMapper.createObjectNode();
        
        // Metadata
        root.put("version", JsonFontFormat.CURRENT_VERSION);
        root.put("format", JsonFontFormat.FORMAT);
        root.put("description", "Character width mappings for Minecraft fonts. Use https://r12a.github.io/app-conversion/ (JS/Java/C category, ES6 disabled) to convert between \\u format and visual representation.");
        
        // Pack default width
        jsonPutNumber(root, "default_width", packInfo.getDefaultWidth());
        
        // Fonts array
        ArrayNode fontsArray = root.putArray("fonts");
        
        for (FontInfo font : packInfo.getFonts().values()) {
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
                ObjectNode widthsNode = fontNode.putObject("widths");

                for (Map.Entry<Integer, Double> entry : widths.entrySet()) {
                    int codepoint = entry.getKey();
                    double width = entry.getValue();

                    // Use actual character - Jackson will escape non-ASCII as Unicode
                    String key = new String(Character.toChars(codepoint));
                    
                    // Add width value using optimal number representation
                    jsonPutNumber(widthsNode, key, width);
                }
            }
        }
        
        // Write with custom pretty formatting for better array indentation
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        objectMapper.writer(printer).writeValue(outputFile.toFile(), root);
        
        System.out.println("Generated JSON font file: " + outputFile);
        System.out.println("  - " + packInfo.getFonts().size() + " fonts");
        System.out.println("  - Pack default width: " + packInfo.getDefaultWidth());
        
        // Print detailed statistics for each font
        for (FontInfo font : packInfo.getFonts().values()) {
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
}