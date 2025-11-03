package io.calinea.internal;

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
    public static int measureComponent(Component component) {
        if (component == null) {
            return 0;
        }
        
        // Convert component to plain text for now
        String text = PlainTextComponentSerializer.plainText().serialize(component);
        Key fontKey = component.font() != null ? component.font() : Key.key("minecraft:default");

        return measureText(fontKey, text);
    }
    
    /**
     * Measures the pixel width of plain text.
     * 
     * @param text the text to measure
     * @return approximate width in pixels
     */
    private static int measureText(Key fontKey, String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        int totalWidth = 0;
        for (char c : text.toCharArray()) {
            totalWidth += getCharWidth(fontKey, c);
        }
        return totalWidth;
    }
    
    /**
     * Gets the approximate width of a character.
     * 
     * @param c the character
     * @return width in pixels
     */
    private static int getCharWidth(Key fontKey, char c) {

        PackInfo packInfo = Calinea.TMPgetPackInfo();

        return packInfo.getWidth(fontKey, c);
    }
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ComponentMeasurer() {
        throw new UnsupportedOperationException("Utility class");
    }
}