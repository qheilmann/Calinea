package io.calinea.generator.parser.font.providers;

import com.fasterxml.jackson.databind.JsonNode;

import io.calinea.generator.parser.PackPathContext;
import io.calinea.pack.font.FontInfo;
import net.kyori.adventure.key.Key;

import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Parses bitmap font providers and calculates character widths from texture images.
 */
public class BitmapProviderParser implements IProviderParser {
    
    @Override
    public String getType() {
        return "bitmap";
    }
    
    @Override
    public void parse(JsonNode provider, FontInfo fontInfo, PackPathContext context) throws IOException {
        String fileLocation = provider.get("file").asText();
        @Nullable JsonNode chars = provider.get("chars");
        
        if (chars == null || !chars.isArray() || chars.size() == 0) {
            return;
        }
        
        Path texturePath = resolveTexturePath(fileLocation, context);
        if (!Files.exists(texturePath)) {
            System.err.println("Texture file not found: " + texturePath);
            return;
        }
        
        try {
            System.out.println("Processing bitmap font: " + fileLocation + " -> " + texturePath);
            calculateCharacterWidths(texturePath, chars, fontInfo);
            
            // Count processed characters for logging
            int totalChars = 0;
            for (JsonNode row : chars) {
                String rowString = row.asText();
                for (char c : rowString.toCharArray()) {
                    if (c != 0) totalChars++;
                }
            }
            System.out.println("Calculated widths for " + totalChars + " characters from " + chars.size() + " rows");
            
        } catch (Exception e) {
            System.err.println("Failed to calculate character widths for " + texturePath + ": " + e.getMessage());
        }
    }
    
    /**
     * Resolves a resourceLocation texture path (namespace:path) to an absolute file path.
     */
    private Path resolveTexturePath(String resourceLocation, PackPathContext context) {
        Key key = Key.key(resourceLocation);
        String namespace = key.namespace();
        String path = key.value();

        return context.getResourcePackRoot()
            .resolve("assets")
            .resolve(namespace)
            .resolve("textures")
            .resolve(path);
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
                
                // Minecraft adds 1px spacing to bitmap fonts
                int widthWithSpacing = actualWidth + 1;

                fontInfo.setWidth(codepoint, widthWithSpacing);
            }
        }
    }
    
    /**
     * Calculates the width of a single glyph by finding the rightmost column with non-transparent pixels.
     */
    private int calculateGlyphWidth(BufferedImage image, int startX, int startY, int cellWidth, int cellHeight) {
        int rightmostCol = 0;
        
        for (int x = startX + cellWidth - 1; x >= startX; x--) {
            boolean hasPixel = false;
            
            for (int y = startY; y < startY + cellHeight && y < image.getHeight(); y++) {
                if (x < image.getWidth()) {
                    int rgb = image.getRGB(x, y);
                    int alpha = (rgb >> 24) & 0xFF;
                    
                    if (alpha > 0) {
                        hasPixel = true;
                        break;
                    }
                }
            }
            
            if (hasPixel) {
                rightmostCol = x - startX + 1;
                break;
            }
        }
        
        return rightmostCol;
    }

    private int codepointLength(String string) {
        return string.codePointCount(0, string.length());
    }
}
