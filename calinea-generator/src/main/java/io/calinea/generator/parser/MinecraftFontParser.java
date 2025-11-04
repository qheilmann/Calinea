package io.calinea.generator.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.Nullable;

import io.calinea.models.FontInfo;
import io.calinea.models.PackInfo;
import net.kyori.adventure.key.Key;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parses Minecraft resource pack fonts and extracts character width information.
 */
public class MinecraftFontParser {
    private final ObjectMapper objectMapper;
    
    public MinecraftFontParser() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Parses all fonts from a resource pack directory
     */
    public PackInfo parseResourcePack(Path resourcePackPath) throws IOException {
        List<FontInfo> fonts = new ArrayList<>();
        
        Path assetsDir = resourcePackPath.resolve("assets");
        if (!Files.exists(assetsDir)) {
            throw new IOException("Assets directory not found: " + assetsDir);
        }
        
        // Scan all namespaces under assets/
        Files.list(assetsDir)
             .filter(Files::isDirectory)
             .forEach(namespaceDir -> {
                 Path fontDir = namespaceDir.resolve("font");
                 if (Files.exists(fontDir)) {
                     try {
                         // Recursively find all .json font files in this namespace's font directory
                         Files.walk(fontDir)
                              .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".json"))
                              .forEach(fontFile -> {
                                  try {
                                      FontInfo fontInfo = parseFontFile(fontFile);
                                      if (fontInfo != null) {
                                          fonts.add(fontInfo);
                                          System.out.println("Parsed font: " + fontInfo.getFontKey().asString() + " from " + fontFile);
                                      }
                                  } catch (Exception e) {
                                      System.err.println("Failed to parse font file " + fontFile + ": " + e.getMessage());
                                  }
                              });
                     } catch (IOException e) {
                         System.err.println("Failed to scan font directory " + fontDir + ": " + e.getMessage());
                     }
                 }
             });
        
        if (fonts.isEmpty()) {
            System.out.println("No font files found in resource pack: " + resourcePackPath);
        } else {
            System.out.println("Found " + fonts.size() + " font(s) in resource pack");
        }

        PackInfo packInfo = new PackInfo(fonts);
        
        // Validate that all font references exist in the parsed font collection
        validateFontReferences(packInfo);
        
        return packInfo;
    }
    
    /**
     * Parses a single font definition file.
     */
    public FontInfo parseFontFile(Path fontFile) throws IOException {
        JsonNode root = objectMapper.readTree(fontFile.toFile());

        Key fontKey = resolveFontKey(fontFile);
        FontInfo fontInfo = new FontInfo(fontKey);
        
        @Nullable JsonNode providers = root.get("providers");
        if (providers != null && providers.isArray()) {
            for (JsonNode provider : providers) {
                parseProvider(provider, fontInfo, fontFile.getParent());
            }
        }
        
        return fontInfo;
    }
    
    private void parseProvider(JsonNode provider, FontInfo fontInfo, Path fontDir) throws IOException {
        String type = provider.get("type").asText();
        
        switch (type) {
            case "bitmap":
                parseBitmapProvider(provider, fontInfo, fontDir);
                break;
            case "reference":
                parseReferenceProvider(provider, fontInfo);
                break;
            case "space":
                parseSpaceProvider(provider, fontInfo);
                break;
            default:
                System.out.println("WARNING: Unsupported provider type: " + type);
        }
    }
    
    private void parseBitmapProvider(JsonNode provider, FontInfo fontInfo, Path fontDir) throws IOException {
        // Parse bitmap font provider
        String fileLocation = provider.get("file").asText();
        @Nullable JsonNode chars = provider.get("chars");
        
        if (chars == null || !chars.isArray() || chars.size() == 0) {
            return;
        }
        
        // Parse the file path: namespace:path format
        Path texturePath = resolveTexturePath(fileLocation, fontDir);
        if (!Files.exists(texturePath)) {
            System.err.println("Texture file not found: " + texturePath);
            return;
        }
        
        // Load the texture and calculate character widths
        try {
            System.out.println("Processing bitmap font: " + fileLocation + " -> " + texturePath);
            calculateCharacterWidths(texturePath, chars, fontInfo);
            
            // Count processed characters for logging
            int totalChars = 0;
            for (JsonNode row : chars) {
                String rowString = row.asText();
                for (char c : rowString.toCharArray()) {
                    if (c != 0) totalChars++; // Skip null characters
                }
            }
            System.out.println("Calculated widths for " + totalChars + " characters from " + chars.size() + " rows");
            
        } catch (Exception e) {
            System.err.println("Failed to calculate character widths for " + texturePath + ": " + e.getMessage());
        }
    }
    
    private void parseReferenceProvider(JsonNode provider, FontInfo fontInfo) throws IOException {
        String fontId = provider.get("id").asText();
        Key referencedFontKey = Key.key(fontId);
        
        // Add the reference to this font (no need to parse or merge immediately)
        fontInfo.addReference(referencedFontKey);

        System.out.println("Added font reference: " + fontId + " -> " + referencedFontKey);
    }
    
    private void parseSpaceProvider(JsonNode provider, FontInfo fontInfo) throws IOException {
        @Nullable JsonNode advances = provider.get("advances");
        
        if (advances == null || !advances.isObject()) {
            System.err.println("Space provider missing or invalid 'advances' object");
            return;
        }
        
        // Iterate through all advances entries
        advances.fieldNames().forEachRemaining(charString -> {
            int codepoint = charString.codePointAt(0);
            double width = advances.get(charString).asDouble();
            fontInfo.setWidth(codepoint, width);
        });
             
        System.out.println("Processed space provider: " + advances.size() + " advance entries");
    }
    
    /**
     * Resolves a resourceLocation texture path (namespace:path) to an absolute file path.
     * Example: "ttt:font/symbols/energy.png" -> "assets/ttt/textures/font/symbols/energy.png"
     */
    private Path resolveTexturePath(String resourceLocation, Path fontDir) {
        Key key = Key.key(resourceLocation);
        String namespace = key.namespace();
        String path = key.value();

        Path resourcePackRoot = findResourcePackRoot(fontDir);
        return resourcePackRoot.resolve("assets").resolve(namespace).resolve("textures").resolve(path);
    }
    
    /**
     * Resolves the font key from a font file path.
     * Example: path/to/resourcepack/assets/ttt/font/symbols/energy.json -> Key("ttt", "symbols/energy")
     */
    private Key resolveFontKey(Path fontFile) {
        Path resourcePackRoot = findResourcePackRoot(fontFile.getParent());
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
        
        // Build the font path from the font directory onwards (excluding assets/<namespace>/font/)
        StringBuilder fontPathBuilder = new StringBuilder();
        for (int i = 3; i < relativePath.getNameCount(); i++) {
            // Append path separator if not the first component
            if (fontPathBuilder.length() > 0) {
                fontPathBuilder.append("/");
            }
            
            String pathComponent = relativePath.getName(i).toString();
            
            // Remove .json extension from the last component (filename)
            String jsonExt = ".json";
            if (i == relativePath.getNameCount() - 1 && pathComponent.endsWith(jsonExt)) {
                pathComponent = pathComponent.substring(0, pathComponent.length() - jsonExt.length());
            }
            
            fontPathBuilder.append(pathComponent);
        }
        
        return Key.key(namespace, fontPathBuilder.toString());
    }
    
    /**
     * Finds the resource pack root by locating pack.mcmeta, walking up from the given directory.
     * 
     * @param startDir The directory to start searching from
     * @return The path to the resource pack root directory
     * @throws IllegalStateException if pack.mcmeta cannot be found in any parent directory
     */
    private Path findResourcePackRoot(Path startDir) {
        Path current = startDir;
        while (current != null) {
            if (Files.exists(current.resolve("pack.mcmeta"))) {
                return current;
            }
            current = current.getParent();
        }
        
        throw new IllegalStateException("Could not find pack.mcmeta while searching ancestors of: " + startDir);
    }
    
    /**
     * Calculates character widths by analyzing the texture bitmap.
     */
    private void calculateCharacterWidths(Path texturePath, JsonNode chars, FontInfo fontInfo) throws IOException {
        BufferedImage image = ImageIO.read(texturePath.toFile());
        
        int rows = chars.size();
        if (rows == 0) return;
        
        // Find the maximum number of characters per row
        int maxCols = 0;
        for (JsonNode row : chars) {
            String rowString = row.asText();
            maxCols = Math.max(maxCols, codepointLength(rowString));
        }
        
        if (maxCols == 0) return;
        
        // Calculate dimensions for each character cell
        int cellWidth = image.getWidth() / maxCols;
        int cellHeight = image.getHeight() / rows;
        
        // Process each character
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            String rowString = chars.get(rowIndex).asText();
            
            for (int colIndex = 0; colIndex < codepointLength(rowString); colIndex++) {
                int codepointIndex = rowString.offsetByCodePoints(0, colIndex);
                int codepoint = rowString.codePointAt(codepointIndex);

                // Skip null characters (U+0000)
                if (codepoint == 0) continue;
                
                // Calculate the actual width by finding the rightmost non-transparent pixel
                int actualWidth = calculateGlyphWidth(image, colIndex * cellWidth, rowIndex * cellHeight, cellWidth, cellHeight);
                
                // Set the width in the font info
                fontInfo.setWidth(codepoint, actualWidth);
            }
        }
    }
    
    /**
     * Calculates the width of a single glyph by finding the rightmost column with non-transparent pixels.
     */
    private int calculateGlyphWidth(BufferedImage image, int startX, int startY, int cellWidth, int cellHeight) {
        int rightmostCol = 0;
        
        // Scan from right to left to find the rightmost column with non-transparent pixels
        for (int x = startX + cellWidth - 1; x >= startX; x--) {
            boolean hasPixel = false;
            
            for (int y = startY; y < startY + cellHeight && y < image.getHeight(); y++) {
                if (x < image.getWidth()) {
                    int rgb = image.getRGB(x, y);
                    int alpha = (rgb >> 24) & 0xFF;
                    
                    if (alpha > 0) { // Non-transparent pixel found
                        hasPixel = true;
                        break;
                    }
                }
            }
            
            if (hasPixel) {
                rightmostCol = x - startX + 1; // +1 because we want width, not index
                break;
            }
        }
        
        return rightmostCol;
    }

    private int codepointLength(String string) {
        return string.codePointCount(0, string.length());
    }
    
    /**
     * Validates that all font references point to fonts that exist in the pack.
     * Prints warnings for any missing references.
     */
    private void validateFontReferences(PackInfo packInfo) {
        Set<Key> availableFonts = packInfo.getFonts().keySet();
        Set<Key> missingReferences = new HashSet<>();
        
        // Check each font's references
        for (FontInfo fontInfo : packInfo.getFonts().values()) {
            for (Key reference : fontInfo.getReferences()) {
                if (!availableFonts.contains(reference)) {
                    missingReferences.add(reference);
                }
            }
        }
        
        // Print warnings for missing references
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