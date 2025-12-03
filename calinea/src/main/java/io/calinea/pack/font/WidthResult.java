package io.calinea.pack.font;

/**
 * Wrapper class to distinguish between valid widths (including negative) and error states.
 */
public class WidthResult {

    private final double width;
    private final Status status;

    public static WidthResult found(double width) {
        return new WidthResult(width, Status.FOUND);
    }

    public static WidthResult missingWidth() {
        return new WidthResult(0, Status.MISSING_WIDTH);
    }

    public static WidthResult missingFont() {
        return new WidthResult(0, Status.MISSING_FONT);
    }

    public static WidthResult circularReference() {
        return new WidthResult(0, Status.CIRCULAR_REFERENCE);
    }

    public double getWidth() {
        return width;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isValid() {
        return status == Status.FOUND;
    }

    private WidthResult(double width, Status status) {
        this.width = width;
        this.status = status;
    }

    /**
     * Enum for width lookup status.
     */
    public enum Status {
        FOUND, MISSING_WIDTH, MISSING_FONT, CIRCULAR_REFERENCE
    }
}