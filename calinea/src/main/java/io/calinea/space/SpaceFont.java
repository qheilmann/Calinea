package io.calinea.space;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import net.kyori.adventure.key.Key;

public class SpaceFont {

    public static final String NAMESPACE = "spacefont";

    public static final int MAX_WIDTH = 8192;
    public static final int MIN_WIDTH = -MAX_WIDTH;
    public static final int FRACTIONAL_DENOMINATOR = 4800;
    public static final double MAX_DELTA = 1.0 / FRACTIONAL_DENOMINATOR;

    private static final int INTEGER_SPACE_ZERO = 0xD0000; // Base codepoint for integer space characters
    private static final int FRACTIONAL_SPACE_ZERO = 0x50000; // Base codepoint for fractional space characters
    private static final String FRACTIONAL_DENOMINATOR_STRING = Integer.toString(FRACTIONAL_DENOMINATOR);

    private static final Map<Key, String> MAP = new HashMap<>();
    private static final Map<String, Key> REVERSE_MAP = new HashMap<>();

    static {
        for (Special character : Special.values()) {
            MAP.put(character.key(), character.codepoint());
            REVERSE_MAP.put(character.codepoint(), character.key());
        }
    }

    /**
     * Creates a Key with the spacefont namespace.
     *
     * @param key the key to use
     * @return a Key with the spacefont namespace
     */
    public static Key key(String key) {
        return Key.key(NAMESPACE, key);
    }

    /**
     * Returns the Unicode codepoint string for the given Key.
     * <p>
     * Supported key formats:
     * </p>
     * <ul>
     *     <li>{@code spacefont:space.<width>} - integer space with specified width</li>
     *     <li>{@code spacefont:space.[-]<n>/4800} - fractional space with numerator n and denominator 4800</li>
     *     <li>{@code spacefont:space.infinity} - positive infinity space</li>
     *     <li>{@code spacefont:space.-infinity} - negative infinity space</li>
     *     <li>{@code spacefont:space.min} - minimum width space</li>
     *     <li>{@code spacefont:space.max} - maximum width space</li>
     *     <li>{@code spacefont:newlayer} - new layer character</li>
     * </ul>
     * <p>
     * <strong>Note:</strong> The format {@code space.[-]<x>/<y>} with arbitrary denominators is not supported.
     * </p>
     * <p>
     * <strong>Performance tip:</strong> Prefer using {@link #space(double)} if you already have a double value
     * to avoid string parsing overhead.
     * </p>
     *
     * @param key the Key to resolve
     * @return the Unicode codepoint string (possibly as a surrogate pair), or {@code null} if the key is not recognized
     */
    public static String getCodepoint(Key key) {
        if (!isValidNamespace(key)) {
            return null;
        }

        String special = MAP.get(key);
        if (special != null) {
            return special;
        }

        if (key.value().startsWith("space.")) {
            return spaceByKey(key);
        }

        return null;
    }
    
    /**
     * Returns the Key for the given Unicode codepoint string.
     * <p>
     * Uses {@code Character.codePointCount} to ensure the string contains a single codepoint.
     * Throws an exception if the string contains multiple codepoints.
     * </p>
     *
     * @param codepoint a string containing a single Unicode codepoint
     * @return the Key corresponding to the codepoint, or {@code null} if not a space codepoint
     * @throws IllegalArgumentException if the string contains multiple codepoints
     * @see #getKey(String, boolean)
     */
    public static Key getKey(String codepoint) {
        return getKey(codepoint, true);
    }

    /**
     * Returns the Key for the first Unicode codepoint in the given string.
     * <p>
     * Uses {@code Character.codePointCount} to count the number of codepoints in the string
     * and {@code Character.codePointAt} to extract the first codepoint.
     * </p>
     *
     * @param codepoint a string containing one or more Unicode codepoints
     * @param singleCodepointOrThrow if {@code true}, throws an exception when the string contains multiple codepoints;
     *                               if {@code false}, processes only the first codepoint
     * @return the Key corresponding to the first codepoint, or {@code null} if not a space codepoint
     * @throws IllegalArgumentException if {@code singleCodepointOrThrow} is {@code true} and the string contains multiple codepoints,
     *                                  or if the codepoint value is invalid
     */
    public static Key getKey(String codepoint, boolean singleCodepointOrThrow) {
        if (codepoint == null || codepoint.isEmpty()) {
            return null;
        }

        char[] chars = codepoint.toCharArray();
        int count = Character.codePointCount(chars, 0, codepoint.length());
        if (count > 1 && singleCodepointOrThrow) {
            throw new IllegalArgumentException("Codepoint string must be a single codepoint: " + codepoint);
        }

        // Check for special cases
        Key special = REVERSE_MAP.get(codepoint);
        if (special != null) {
            return special;
        }

        // convert "\u0020" or even "\uD83C\uDDF7" to codepoint
        int codepointValue = Character.codePointAt(chars, 0);
        if (!Character.isValidCodePoint(codepointValue)) {
            throw new IllegalArgumentException("Invalid codepoint: 0x" + Integer.toString(codepointValue, 16));
        }

        // Check for integer space
        if(isIntegerSpaceCodepoint(codepointValue)) {
            return key("space." + Integer.toString(codepointValue - INTEGER_SPACE_ZERO));
        }
        // Check for fractional space
        else if (isFractionalSpaceCodepoint(codepointValue)) {
            String numerator = Integer.toString(codepointValue - FRACTIONAL_SPACE_ZERO);
            return key("space." + numerator + "/" + FRACTIONAL_DENOMINATOR_STRING);
        }

        return null;
    }

    /**
     * Returns Unicode space character(s) that best approximate the given width.
     * <p>
     * The method selects the optimal representation based on the following rules:
     * </p>
     * <ul>
     *   <li>If {@code width < MIN_WIDTH}: returns {@link Special#NEGATIVE_INFINITY} codepoint</li>
     *   <li>If {@code width > MAX_WIDTH}: returns {@link Special#POSITIVE_INFINITY} codepoint</li>
     *   <li>If the width is close to an integer ({@code Math.abs(fractionalPart) < MAX_DELTA}): returns integer space character</li>
     *   <li>If width is between -1 and 1 ({@code integerPart == 0}): returns fractional space character which never exceeds the requested width
     *       (rounded down to the nearest 1/{@link #FRACTIONAL_DENOMINATOR}, with a maximum error of 1/{@link #MAX_DELTA} px
     *       and an average error of {@link #MAX_DELTA}/2 px in case of an uniform distribution)</li>
     *   <li>Otherwise: returns both integer and fractional space characters concatenated</li>
     * </ul>
     *
     * @param width the desired space width offset in pixels
     * @return a string containing one or two Unicode space characters, possibly as surrogate pairs (e.g., \\uXXXX\\uXXXX)
     */
    public static String space(double width) {

        // Extreme
        if (width < MIN_WIDTH) {
            return Special.NEGATIVE_INFINITY.codepoint();
        } else if (width > MAX_WIDTH) {
            return Special.POSITIVE_INFINITY.codepoint();
        }

        int integerPart = BigDecimal.valueOf(width).setScale(0, RoundingMode.DOWN).intValue();
        double fractionalPart = width - integerPart;

        // Only integer part is significant
        if (Math.abs(fractionalPart) < MAX_DELTA) {
            return spaceInteger(integerPart);
        }

        // Only fractional part is significant
        if (integerPart == 0) {
            return spaceFractional(fractionalPart);
        }
        
        // Combine if both are significant
        return spaceInteger(integerPart) + spaceFractional(fractionalPart);
    }

    /**
     * Offsets a string by a given amount and then returns it back by the same amount.
     * <p>
     * This method wraps the provided string with space characters: first applying a positive offset,
     * then including the string, and finally applying the same negative offset.
     * Useful for dynamically adjusting the position of a string without affecting surrounding elements.
     * </p>
     * <p>
     * <strong>Note:</strong> The position before and after this string is not the same—it will be offset
     * by the width of the string itself.
     * </p>
     *
     * @param value the desired offset amount in pixels
     * @param offsetString the string to be offset
     * @return a string containing the offset spaces, the provided string, and the return offset spaces
     */
    public static String offset(double value, String offsetString) {
        return space(value) + offsetString + space(-value);
    }

    //#region Helpers

    // space.<width>
    // space.[-]<n>/4800
    // space.[-]<x>/<y> // not implemented
    private static String spaceByKey(Key key) {
        if (!key.value().startsWith("space.")) {
            return null;
        }

        String suffix = key.value().substring("space.".length());

        if (suffix.contains("/")) {
            // Handle fractional space keys, format: space.[-]<n>/4800
            return handleFractionalBySuffix(suffix);

        } else {
            // Handle integer space keys, format: space.<width>
            return handleIntegerBySuffix(suffix);
        }
    }

    // [-]<n>/4800 from space.[-]<n>/4800
    private static String handleFractionalBySuffix(String suffix){
        String[] parts = suffix.split("/");
        if (parts.length != 2) {
            return null;
        }

        if (!parts[1].equals(FRACTIONAL_DENOMINATOR_STRING)) {
            return null;
        }

        int numerator;
        if (parts[0].matches("-?\\d+")) { // Check if the string is a valid integer (faster than try-catch)
            numerator = Integer.parseInt(parts[0]);
        } else {
            return null;
        }

        return spaceFractional(numerator / (double) FRACTIONAL_DENOMINATOR);
    }
    // <width> from space.<width>
    private static String handleIntegerBySuffix(String suffix){
            int width;
            if (suffix.matches("-?\\d+")) { // Check if the string is a valid integer (faster than try-catch)
                width = Integer.parseInt(suffix);
            } else {
                return null;
            }

            return spaceInteger(width);
    }


    // The font pack encodes integer spaces by adding the desired width to INTEGER_SPACE_ZERO.
    // Minecraft interprets Unicode escape sequences (e.g., \\uXXXX or \\uXXXX\\uXXXX) and displays the corresponding character.
    // For codepoints above 0xFFFF, surrogate pairs are needed to ensure proper representation.
    private static String spaceInteger(int width) {
        if (!isValidIntegerWidth(width)) {
            return null;
        }
        int codepoint = INTEGER_SPACE_ZERO + width;
        return asSurrogatePair(codepoint);
    }

    // The font pack encodes fractional spaces by adding the round(width*FRACTIONAL_DENOMINATOR) to FRACTIONAL_SPACE_ZERO.
    // Minecraft interprets Unicode escape sequences (e.g., \\uXXXX or \\uXXXX\\uXXXX) and displays the corresponding character.
    // For codepoints above 0xFFFF, surrogate pairs are needed to ensure proper representation.
    private static String spaceFractional(double width) {
        if (!isValidFractionalWidth(width)) {
            return null;
        }
        // Round to nearest 1/FRACTIONAL_DENOMINATOR using RoundingMode.DOWN
        int numerator = BigDecimal.valueOf(width * FRACTIONAL_DENOMINATOR).setScale(0, RoundingMode.DOWN).intValue();
        int codepoint = FRACTIONAL_SPACE_ZERO + numerator;
        return asSurrogatePair(codepoint);
    }

    private static String asSurrogatePair(int codepoint) {
        // Converts a codepoint to a surrogate pair string representation if needed.
        char[] surrogates = Character.toChars(codepoint);
        return new String(surrogates);
    }

    private static boolean isValidIntegerWidth(int width) {
        return width >= MIN_WIDTH && width <= MAX_WIDTH;
    }

    private static boolean isValidFractionalWidth(double width) {
        return width >= -1 && width <= 1;
    }

    private static boolean isIntegerSpaceCodepoint(int codepoint) {
        return codepoint >= INTEGER_SPACE_ZERO + MIN_WIDTH && codepoint <= INTEGER_SPACE_ZERO + MAX_WIDTH; // MIN_WIDTH/MAX_WIDTH are relative value, we need to sum it
    }

    private static boolean isFractionalSpaceCodepoint(int codepoint) {
        return codepoint >= FRACTIONAL_SPACE_ZERO - FRACTIONAL_DENOMINATOR && codepoint <= FRACTIONAL_SPACE_ZERO + FRACTIONAL_DENOMINATOR;
    }

    private static boolean isValidNamespace(Key key) {
        return key.namespace().equals(NAMESPACE);
    }

    //#endregion Helpers

    public enum Special {
        ZERO(SpaceFont.key("space.0"), spaceInteger(0)),
        MIN(SpaceFont.key("space.min"), spaceInteger(MIN_WIDTH)),
        MAX(SpaceFont.key("space.max"), spaceInteger(MAX_WIDTH)),
        NEGATIVE_INFINITY(SpaceFont.key("space.-infinity"), asSurrogatePair(0xC0001)),
        POSITIVE_INFINITY(SpaceFont.key("space.infinity"), asSurrogatePair(0xDFFFF)),
        NEWLAYER(SpaceFont.key("newlayer"), asSurrogatePair(0xC0000));

        private final Key key;
        private final String codepoint;

        Special(Key key, String codepoint) {
            this.key = key;
            this.codepoint = codepoint;
        }

        /**
         * Returns the Key associated with this special character.
         *
         * @return the Key for this special character
         */
        public Key key() {
            return key;
        }

        /**
         * Returns the Unicode codepoint string for this special character.
         *
         * @return the codepoint string, possibly as a surrogate pair
         */
        public String codepoint() {
            return codepoint;
        }

        /**
         * Prefer using the {@link #codepoint()} method for better readability.
         * This method allows the enum to be used directly as a string wherever a toString() can be used implicitly.
         */
        @Override
        public String toString() {
            return codepoint();
        }
    }
}
