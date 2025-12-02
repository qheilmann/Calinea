package io.calinea.generator.writer;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility class for common JSON node operations.
 * Provides helper methods for writing values with optimal representations.
 */
public final class JsonNodeHelper {
    
    private JsonNodeHelper() {
        // Utility class, no instantiation
    }
    
    /**
     * Checks if a double value represents a whole number.
     * 
     * @param value the value to check
     * @return true if the value is a whole number within integer range
     */
    public static boolean isWholeNumber(double value) {
        // Check for NaN and infinite values
        if (!Double.isFinite(value)) {
            return false;
        }
        
        // Get the rounded value
        long roundedValue = Math.round(value);
        
        // Check if the rounded value is within the safe integer range
        if (roundedValue < Integer.MIN_VALUE || roundedValue > Integer.MAX_VALUE) {
            return false;
        }
        
        // Check if the value is close enough to the rounded value
        // Using epsilon for floating-point comparison safety
        double epsilon = 1e-10;
        return Math.abs(value - roundedValue) < epsilon;
    }
    
    /**
     * Adds a numeric value to a JSON ObjectNode, using integer representation
     * for whole numbers and double representation for fractional values.
     * 
     * @param node the ObjectNode to add the value to
     * @param key the key for the value
     * @param value the numeric value to add
     */
    public static void putNumber(ObjectNode node, String key, double value) {
        if (isWholeNumber(value)) {
            node.put(key, (int) Math.round(value));
        } else {
            node.put(key, value);
        }
    }
}
