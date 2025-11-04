package io.calinea.internal;

import org.jspecify.annotations.Nullable;

import io.calinea.Calinea;
import io.calinea.models.PackInfo;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Internal component measurement utilities.
 * 
 * WARNING: This is internal API and can change without notice.
 * Do NOT use this class directly in your code!
 * 
 * This class handles the complex logic of measuring Adventure components,
 * taking into account fonts, styles, and formatting.
 */
public class ComponentMeasurer {
      
    /**
     * Measures the pixel width of a component.
     * 
     * This is a simplified implementation. A real implementation would:
     * - Load actual font metrics from resource pack
     * - Handle different font families
     * - Account for bold/italic styles
     * - Handle complex components with children
     * 
     * @param component the component to measure
     * @return approximate width in pixels
     */
    public static double measureComponent(Component component) {
        // Convert component to plain text for now
        String text = PlainTextComponentSerializer.plainText().serialize(component);
        @Nullable Key font = component.font();
        Key fontKey = font != null ? font : Key.key("minecraft:default");

        return measureText(fontKey, text);
    }
    
    /**
     * Measures the pixel width of plain text.
     * 
     * @param text the text to measure
     * @return approximate width in pixels
     */
    private static double measureText(Key fontKey, String text) {
        if (text.isEmpty()) {
            return 0;
        }
        
        double totalWidth = 0;

        int codepointLength = text.codePointCount(0, text.length());
        for (int colIndex = 0; colIndex < codepointLength; colIndex++) {
            int codepointIndex = text.offsetByCodePoints(0, colIndex);
            int codepoint = text.codePointAt(codepointIndex);

            totalWidth += getCharWidth(fontKey, codepoint);
        }

        return totalWidth;
    }
    
    /**
     * Gets the approximate width of a character.
     * 
     * @param c the character
     * @return width in pixels
     */
    private static double getCharWidth(Key fontKey, int codepoint) {
        @Nullable PackInfo packInfo = Calinea.TMPgetPackInfo();
        
        if (packInfo == null) {
            // Fallback to default width if pack info is not loaded yet
            return PackInfo.DEFAULT_CHAR_WIDTH;
        }

        return packInfo.getWidth(fontKey, codepoint);
    }
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ComponentMeasurer() {
        throw new UnsupportedOperationException("Utility class");
    }
}