package io.calinea.pack.reader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.calinea.pack.PackInfo;
import io.calinea.pack.font.FontsInfo;
import io.calinea.pack.keybind.KeybindsInfo;
import io.calinea.pack.translation.TranslationsInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.SequencedCollection;

/**
 * Main reader for Calinea config JSON files.
 */
public class JsonPackReader {
    
    public static final String FORMAT = "calinea-config";
    public static final SequencedCollection<Integer> SUPPORTED_VERSIONS = List.of(1);
    public static final int CURRENT_VERSION = SUPPORTED_VERSIONS.getLast();
    
    private final ObjectMapper objectMapper;
    
    public JsonPackReader() {
        JsonFactory factory = JsonFactory.builder()
            .configure(JsonWriteFeature.ESCAPE_NON_ASCII, true)
            .build();
        this.objectMapper = new ObjectMapper(factory);
    }
    
    /**
     * Reads a Calinea config file and returns all parsed data.
     *
     * @param jsonFile path to the JSON config file
     * @return PackInfo containing fonts, keybinds, and translations
     * @throws IOException if reading or parsing fails
     */
    public PackInfo read(Path jsonFile) throws IOException {
        JsonNode root = objectMapper.readTree(jsonFile.toFile());
        
        validateFormat(root);
        int version = validateVersion(root);
        
        return switch (version) {
            case 1 -> readV1(root);
            default -> throw new IOException("Unsupported version: " + version);
        };
    }
    
    private void validateFormat(JsonNode root) throws IOException {
        String format = root.path("format").asText("");
        if (!format.equals(FORMAT)) {
            throw new IOException("Invalid format: expected '" + FORMAT + "', got '" + format + "'");
        }
    }
    
    private int validateVersion(JsonNode root) throws IOException {
        int version = root.path("version").asInt(-1);
        if (!SUPPORTED_VERSIONS.contains(version)) {
            throw new IOException("Unsupported version: " + version + ". Supported: " + SUPPORTED_VERSIONS);
        }
        return version;
    }
    
    private PackInfo readV1(JsonNode root) throws IOException {
        return new PackInfo(
            readSection(root, new FontsSectionReader(), new FontsInfo()),
            readSection(root, new KeybindsSectionReader(), new KeybindsInfo()),
            readSection(root, new TranslationsSectionReader(), TranslationsInfo.empty())
        );
    }
    
    /**
     * Reads an optional section from the root node.
     * Returns the default value if the section is not present.
     */
    private <T> T readSection(JsonNode root, ISectionReader<T> reader, T defaultValue) throws IOException {
        JsonNode sectionNode = root.get(reader.getSectionName());
        if (sectionNode == null) {
            return defaultValue;
        }
        return reader.read(sectionNode);
    }
}
