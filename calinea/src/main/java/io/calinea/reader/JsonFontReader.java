package io.calinea.reader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;

import org.jspecify.annotations.Nullable;

import io.calinea.models.FontInfo;
import io.calinea.models.PackInfo;
import net.kyori.adventure.key.Key;

public class JsonFontReader {

        private final ObjectMapper objectMapper;
    
    public JsonFontReader() {
        JsonFactory factory = JsonFactory.builder()
            .configure(JsonWriteFeature.ESCAPE_NON_ASCII, true)
            .build();
        this.objectMapper = new ObjectMapper(factory);
    }

    /**
     * Reads fonts from a JSON file.
     */
    public PackInfo readFonts(Path jsonFile) throws IOException {
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

    private PackInfo V1 (JsonNode root) throws IOException {
        JsonNode defaultWidthNode = root.path("default_width");
        if (defaultWidthNode.isMissingNode() || !defaultWidthNode.isInt()) {
            throw new IOException("Missing or invalid 'default_width' in JSON file");
        }
        double defaultWidth = defaultWidthNode.asDouble();

        @Nullable JsonNode fontsArray = root.get("fonts");
        if (fontsArray == null || !fontsArray.isArray()) {
            throw new IOException("Missing or invalid 'fonts' array in JSON file");
        }
        
        PackInfo packInfo = new PackInfo(defaultWidth);
        
        for (JsonNode fontNode : fontsArray) {
            @Nullable JsonNode fontKeyNode = fontNode.get("fontKey");
            if (fontKeyNode == null) {
                throw new IOException("Missing 'fontKey' in font entry");
            }
            String rawFontKey = fontKeyNode.asText();
            if (rawFontKey.isEmpty()) {
                throw new IOException("Missing or invalid 'fontKey' in font entry");
            }
            Key fontKey = Key.key(rawFontKey);
            FontInfo fontInfo = new FontInfo(fontKey);
            
            // Parse references (if any)
            @Nullable JsonNode referencesNode = fontNode.get("references");
            if (referencesNode != null && referencesNode.isArray()) {
                for (JsonNode referenceNode : referencesNode) {
                    String referenceKeyString = referenceNode.asText();
                    Key referenceKey = Key.key(referenceKeyString);
                    fontInfo.addReference(referenceKey);
                }
            }
            
            // Parse widths (if any)
            @Nullable JsonNode widthsNode = fontNode.get("widths");
            if (widthsNode != null && widthsNode.isObject()) {
                widthsNode.fields().forEachRemaining(entry -> {
                    String key = entry.getKey();
                    double width = entry.getValue().asDouble();
                    
                    // Parse codepoint from key
                    int codepoint = key.codePointAt(0);

                    if (codepoint >= 0) {
                        fontInfo.setWidth(codepoint, width);
                    }
                });
            }
            
            packInfo.addFont(fontInfo);
        }
        
        return packInfo;
    }
}
