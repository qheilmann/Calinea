package io.calinea.generator.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.calinea.models.FontInfo;
import io.calinea.models.PackInfo;
import net.kyori.adventure.key.Key;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        
        Path fontsDir = resourcePackPath.resolve("assets/minecraft/font");
        if (!Files.exists(fontsDir)) {
            throw new IOException("Font directory not found: " + fontsDir);
        }
        
        // Look for font definition files (*.json)
        Files.list(fontsDir)
             .filter(path -> path.toString().endsWith(".json"))
             .forEach(fontFile -> {
                 try {
                     FontInfo fontInfo = parseFontFile(fontFile);
                     if (fontInfo != null) {
                         fonts.add(fontInfo);
                     }
                 } catch (Exception e) {
                     System.err.println("Failed to parse font file " + fontFile + ": " + e.getMessage());
                 }
             });
        

        PackInfo packInfo = new PackInfo(fonts);
        return packInfo;
    }
    
    /**
     * Parses a single font definition file.
     */
    public FontInfo parseFontFile(Path fontFile) throws IOException {
        JsonNode root = objectMapper.readTree(fontFile.toFile());

        Key fontKey = resolveFontKey(fontFile);
        FontInfo fontInfo = new FontInfo(fontKey);
        
        JsonNode providers = root.get("providers");
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
            // TODO handle at least the reference provider
            default:
                System.out.println("Unsupported provider type: " + type);
        }
    }
    
    private void parseBitmapProvider(JsonNode provider, FontInfo fontInfo, Path fontDir) throws IOException {
        // Parse bitmap font provider
        String fileLocation = provider.get("file").asText();
        JsonNode chars = provider.get("chars");
        
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
    
    /**
     * Resolves a Minecraft texture path (namespace:path) to an absolute file path.
     * Example: "ttt:font/symbols/energy.png" -> "assets/ttt/textures/font/symbols/energy.png"
     */
    private Path resolveTexturePath(String fileLocation, Path fontDir) {
        String namespace;
        String path;
        
        if (fileLocation.contains(":")) {
            String[] parts = fileLocation.split(":", 2);
            namespace = parts[0];
            path = parts[1];
        } else {
            namespace = "minecraft";
            path = fileLocation;
        }
        
        Path resourcePackRoot = findResourcePackRoot(fontDir);
        return resourcePackRoot.resolve("assets").resolve(namespace).resolve("textures").resolve(path);
    }

    @SuppressWarnings("null")
    private Key resolveFontKey(Path fontFile) {
        String fileName = fontFile.getFileName().toString().replace(".json", "");
        String namespace = extractNamespaceFromFontPath(fontFile);
        
        return Key.key(namespace, fileName);
    }
    
    /**
     * Extracts the namespace from a font file path.
     * The namespace is the directory name directly under assets/ in the path structure:
     * <packroot>/assets/<namespace>/font/<fontKeyName>
     * 
     * @param fontFile The path to the font file
     * @return The namespace (e.g., "minecraft", "ttt")
     * @throws IllegalArgumentException if the path structure is invalid
     */
    private String extractNamespaceFromFontPath(Path fontFile) {
        // Find the resource pack root first
        Path resourcePackRoot = findResourcePackRoot(fontFile.getParent());
        
        // Get the relative path from resource pack root to the font file
        Path relativePath = resourcePackRoot.relativize(fontFile);
        
        // Expected structure: assets/<namespace>/font/<fontfile>.json
        if (relativePath.getNameCount() < 3) {
            throw new IllegalArgumentException("Invalid font file path structure: " + fontFile);
        }
        
        if (!"assets".equals(relativePath.getName(0).toString())) {
            throw new IllegalArgumentException("Font file must be under assets directory: " + fontFile);
        }
        
        if (!"font".equals(relativePath.getName(2).toString())) {
            throw new IllegalArgumentException("Font file must be in font directory: " + fontFile);
        }
        
        return relativePath.getName(1).toString();
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
}