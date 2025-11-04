package io.calinea.config;

import java.nio.file.Path;

import io.calinea.logger.CalineaLogger;
import io.calinea.models.PackInfo;

public class CalineaConfig {
    private Path fontInfoPath = Path.of("./font-widths.json");
    private CalineaLogger logger = CalineaLogger.silent();
    private boolean warnOnMissingWidths = true;
    private boolean warnOnMissingFonts = true;
    private boolean verboseLogging = false;

    public Path fontInfoPath() {
        return fontInfoPath;
    }

    /**
     * Sets the path to the font widths JSON file.
     */
    public CalineaConfig fontInfoPath(Path fontInfoPath) {
        this.fontInfoPath = fontInfoPath;
        return this;
    }

    public CalineaLogger logger() {
        return logger;
    }

    /**
     * Sets the logger implementation to use for logging messages.
     */
    public CalineaConfig logger(CalineaLogger logger) {
        this.logger = logger;
        return this;
    }

    public boolean warnOnMissingWidths() {
        return warnOnMissingWidths;
    }

    /**
     * Sets whether to warn when a character is missing from the font widths data.
     * In all case when a character is missing, it will default to a width of {@link PackInfo#DEFAULT_CHAR_WIDTH}.
     * <p>
     * Note you need to pass a logger implementation with {@link #logger(CalineaLogger)} for warnings to be logged. </p>
     */
    public CalineaConfig warnOnMissingWidths(boolean warnOnMissingFontWidths) {
        this.warnOnMissingWidths = warnOnMissingFontWidths;
        return this;
    }

    public boolean warnOnMissingFonts() {
        return warnOnMissingFonts;
    }

    /**
     * Sets whether to warn when a font is missing from the font widths data.
     * In all case when a font is missing, all its characters will default to a width of {@link PackInfo#DEFAULT_CHAR_WIDTH}.
     * <p>
     * Note you need to pass a logger implementation with {@link #logger(CalineaLogger)} for warnings to be logged. </p>
     */
    public CalineaConfig warnOnMissingFonts(boolean warnOnMissingFonts) {
        this.warnOnMissingFonts = warnOnMissingFonts;
        return this;
    }

    public boolean verboseLogging() {
        return verboseLogging;
    }

    public CalineaConfig verboseLogging(boolean verboseLogging) {
        this.verboseLogging = verboseLogging;
        return this;
    }
}
