package io.calinea.internal;

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
    
    // Approximate character widths for default Minecraft font
    // This is a simplified version - real implementation would load from resource pack
    private static final int DEFAULT_CHAR_WIDTH = 6;
    private static final int SPACE_WIDTH = 4;
    
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
        return measureText(text);
    }
    
    /**
     * Measures the pixel width of plain text.
     * 
     * @param text the text to measure
     * @return approximate width in pixels
     */
    private static int measureText(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        int totalWidth = 0;
        for (char c : text.toCharArray()) {
            totalWidth += getCharWidth(c);
        }
        return totalWidth;
    }
    
    /**
     * Gets the approximate width of a character.
     * 
     * @param c the character
     * @return width in pixels
     */
    private static int getCharWidth(char c) {
        // Simplified character width calculation
        // Real implementation would use actual font metrics
        switch (c) {
            case ' ':
                return SPACE_WIDTH;
            case 'i':
            case 'l':
            case '!':
            case '|':
                return 2;
            case 'I':
            case '[':
            case ']':
            case 't':
                return 4;
            case 'f':
            case 'k':
                return 5;
            default:
                return DEFAULT_CHAR_WIDTH;
        }
    }
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ComponentMeasurer() {
        throw new UnsupportedOperationException("Utility class");
    }
}