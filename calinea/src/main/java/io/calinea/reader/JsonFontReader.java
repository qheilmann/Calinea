package io.calinea.reader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import io.calinea.models.FontInfo;
import net.kyori.adventure.key.Key;

public class JsonFontReader {

        private final ObjectMapper objectMapper;
    
    public JsonFontReader() {
        @SuppressWarnings("null")
        JsonFactory factory = JsonFactory.builder()
            .configure(JsonWriteFeature.ESCAPE_NON_ASCII, true)
            .build();
        this.objectMapper = new ObjectMapper(factory);
    }

    /**
     * Reads fonts from a JSON file.
     */
    public List<FontInfo> readFonts(Path jsonFile) throws IOException {
        JsonNode root = objectMapper.readTree(jsonFile.toFile());
        
        // Verify format
        String format = root.path("format").asText("");
        if (!format.equals(JsonFontFormat.FORMAT)) {
            throw new IOException("Invalid format: expected '" + JsonFontFormat.FORMAT + "', got '" + format + "'");
        }

        // Verify version
        int version = root.path("version").asInt(-1);
        switch (version) {
            case 1:
                return V1(root);
            default:
                throw new IOException("Unsupported version: " + version + ". Supported versions: " + JsonFontFormat.SUPPORTED_VERSIONS);
        }
    }

    private List<FontInfo> V1 (JsonNode root) throws IOException {
         JsonNode fontsArray = root.get("fonts");
        if (fontsArray == null || !fontsArray.isArray()) {
            throw new IOException("Missing or invalid 'fonts' array in JSON file");
        }
        
        List<FontInfo> fonts = new java.util.ArrayList<>();
        
        for (JsonNode fontNode : fontsArray) {
            String rawFontKey = fontNode.get("fontKey").asText();
            if (rawFontKey == null || rawFontKey.isEmpty()) {
                throw new IOException("Missing or invalid 'fontKey' in font entry");
            }
            Key fontKey = Key.key(rawFontKey);
            int defaultWidth = fontNode.get("default_width").asInt(6);
            
            FontInfo fontInfo = new FontInfo(fontKey, defaultWidth);
            
            JsonNode widthsNode = fontNode.get("widths");
            if (widthsNode != null && widthsNode.isObject()) {
                widthsNode.fields().forEachRemaining(entry -> {
                    String key = entry.getKey();
                    int width = entry.getValue().asInt();
                    
                    // Parse codepoint from key
                    int codepoint = key.codePointAt(0);

                    if (codepoint >= 0) {
                        fontInfo.setWidth(codepoint, width);
                    }
                });
            }
            
            fonts.add(fontInfo);
        }
        
        return fonts;
    }
}
