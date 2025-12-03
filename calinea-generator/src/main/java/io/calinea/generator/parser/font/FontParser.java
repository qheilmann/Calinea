package io.calinea.generator.parser.font;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.calinea.generator.parser.IResourceParser;
import io.calinea.generator.parser.PackPathContext;
import io.calinea.generator.parser.font.providers.BitmapProviderParser;
import io.calinea.generator.parser.font.providers.IProviderParser;
import io.calinea.generator.parser.font.providers.ReferenceProviderParser;
import io.calinea.generator.parser.font.providers.SpaceProviderParser;
import io.calinea.pack.font.FontInfo;
import io.calinea.pack.font.FontsInfo;

import org.jspecify.annotations.Nullable;

import net.kyori.adventure.key.Key;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parses Minecraft font definitions and extracts character width information.
 */
public class FontParser implements IResourceParser<FontsInfo> {
    
    private static final String FONT_SUBDIRECTORY = "font";
    
    private final ObjectMapper objectMapper;
    private final Map<String, IProviderParser> providerParsers;
    
    public FontParser() {
        this.objectMapper = new ObjectMapper();
        this.providerParsers = new HashMap<>();
        
        // Register default provider parsers
        registerProvider(new BitmapProviderParser());
        registerProvider(new ReferenceProviderParser());
        registerProvider(new SpaceProviderParser());
    }
    
    /**
     * Registers a provider parser for a specific provider type.
     */
    public void registerProvider(IProviderParser parser) {
        providerParsers.put(parser.getType(), parser);
    }
    
    @Override
    public FontsInfo parse(Path resourcePackRoot) {
        FontsInfo fontsInfo = new FontsInfo();
        Path assetsDir = resourcePackRoot.resolve("assets");
        
        try {
            for (Path namespaceDir : Files.list(assetsDir).filter(Files::isDirectory).toList()) {
                Path fontDir = namespaceDir.resolve(FONT_SUBDIRECTORY);
                if (Files.exists(fontDir)) {
                    fontsInfo.addAll(parseFontDir(fontDir));
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to scan assets directory: " + e.getMessage());
        }
        
        return fontsInfo;
    }
    
    @Override
    public void validate(FontsInfo fonts) {
        validateFontReferences(fonts);
    }
    
    @Override
    public void printStatistics(FontsInfo fonts) {
        if (fonts.getFonts().isEmpty()) {
            System.out.println("No font files found");
        } else {
            System.out.println("Found " + fonts.getFonts().size() + " font(s)");
        }
    }
    
    /**
     * Parses all fonts from a font directory.
     */
    private List<FontInfo> parseFontDir(Path fontDir) {
        List<FontInfo> fonts = new ArrayList<>();
        
        try {
            List<Path> fontFiles = Files.walk(fontDir)
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".json"))
                .toList();
            
            for (Path fontFile : fontFiles) {
                FontInfo fontInfo = tryParseFontFile(fontFile);
                if (fontInfo != null) {
                    fonts.add(fontInfo);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to scan font directory " + fontDir + ": " + e.getMessage());
        }
        
        return fonts;
    }
    
    /**
     * Attempts to parse a font file, returning null and logging errors on failure.
     */
    private FontInfo tryParseFontFile(Path fontFile) {
        try {
            FontInfo fontInfo = parseFontFile(fontFile);
            System.out.println("Parsed font: " + fontInfo.getFontKey().asString() + " from " + fontFile);
            return fontInfo;
        } catch (Exception e) {
            System.err.println("Failed to parse font file " + fontFile + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Parses a single font definition file.
     */
    public FontInfo parseFontFile(Path fontFile) throws IOException {
        JsonNode root = objectMapper.readTree(fontFile.toFile());

        Key fontKey = resolveFontKey(fontFile);
        FontInfo fontInfo = new FontInfo(fontKey);
        
        PackPathContext context = new PackPathContext(fontFile.getParent());
        
        @Nullable JsonNode providers = root.get("providers");
        if (providers != null && providers.isArray()) {
            for (JsonNode provider : providers) {
                parseProvider(provider, fontInfo, context);
            }
        }
        
        return fontInfo;
    }
    
    private void parseProvider(JsonNode provider, FontInfo fontInfo, PackPathContext context) throws IOException {
        String type = provider.get("type").asText();
        
        IProviderParser parser = providerParsers.get(type);
        if (parser != null) {
            parser.parse(provider, fontInfo, context);
        } else {
            System.out.println("WARNING: Unsupported provider type: " + type);
        }
    }
    
    /**
     * Resolves the font key from a font file path.
     */
    private Key resolveFontKey(Path fontFile) {
        Path resourcePackRoot = PackPathContext.findResourcePackRoot(fontFile);
        Path relativePath = resourcePackRoot.relativize(fontFile);
        
        // Expected structure: assets/<namespace>/font/<optionalSubfolder>/<fontfile>.json
        if (relativePath.getNameCount() < 4) {
            throw new IllegalArgumentException("Invalid font file path structure: " + fontFile);
        }
        
        if (!"assets".equals(relativePath.getName(0).toString())) {
            throw new IllegalArgumentException("Font file must be under 'assets' directory: " + fontFile);
        }
        
        if (!"font".equals(relativePath.getName(2).toString())) {
            throw new IllegalArgumentException("Font file must be in 'font' directory: " + fontFile);
        }
        
        String namespace = relativePath.getName(1).toString();
        
        StringBuilder fontPathBuilder = new StringBuilder();
        for (int i = 3; i < relativePath.getNameCount(); i++) {
            if (fontPathBuilder.length() > 0) {
                fontPathBuilder.append("/");
            }
            
            String pathComponent = relativePath.getName(i).toString();
            
            String jsonExt = ".json";
            if (i == relativePath.getNameCount() - 1 && pathComponent.endsWith(jsonExt)) {
                pathComponent = pathComponent.substring(0, pathComponent.length() - jsonExt.length());
            }
            
            fontPathBuilder.append(pathComponent);
        }
        
        return Key.key(namespace, fontPathBuilder.toString());
    }
    
    /**
     * Validates that all font references point to fonts that exist in the current set.
     */
    private void validateFontReferences(FontsInfo fonts) {
        Set<Key> availableFonts = new HashSet<>();
        for (FontInfo fontInfo : fonts.getFonts().values()) {
            availableFonts.add(fontInfo.getFontKey());
        }
        
        Set<Key> missingReferences = new HashSet<>();
        
        for (FontInfo fontInfo : fonts.getFonts().values()) {
            for (Key reference : fontInfo.getReferences()) {
                if (!availableFonts.contains(reference)) {
                    missingReferences.add(reference);
                }
            }
        }
        
        if (!missingReferences.isEmpty()) {
            System.err.println("WARNING: Found " + missingReferences.size() + " missing font reference(s):");
            for (Key missingRef : missingReferences) {
                System.err.println("  - " + missingRef.asString() + " (referenced but not found in pack)");
            }
        } else {
            System.out.println("All font references are valid");
        }
    }
}
