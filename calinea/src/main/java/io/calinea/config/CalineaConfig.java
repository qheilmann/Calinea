package io.calinea.config;

import java.nio.file.Path;

import io.calinea.logger.CalineaLogger;
import io.calinea.pack.font.FontsInfo;

public class CalineaConfig {
    private Path calineaConfigPath = Path.of("./", CalineaGeneratorDefault.DEFAULT_OUTPUT_FILENAME);
    private CalineaLogger logger = CalineaLogger.silent();
    private boolean warnOnMissingWidths = true;
    private boolean warnOnMissingFonts = true;
    private boolean warnOnUnresolvedServerComponents = true;
    private boolean warnOnUnforcedClientComponents = true;
    private boolean verboseLogging = false;

    public Path calineaConfigPath() {
        return calineaConfigPath;
    }

    /**
     * Sets the path to the config JSON file.
     */
    public CalineaConfig calineaConfigPath(Path calineaConfigPath) {
        this.calineaConfigPath = calineaConfigPath;
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
     * Sets whether to warn when a character is missing from the calinea config data.
     * In all case when a character is missing, it will default to a width of {@link FontsInfo#DEFAULT_CHAR_WIDTH}.
     */
    public CalineaConfig warnOnMissingWidths(boolean warnOnMissingFontWidths) {
        this.warnOnMissingWidths = warnOnMissingFontWidths;
        return this;
    }

    public boolean warnOnMissingFonts() {
        return warnOnMissingFonts;
    }

    /**
     * Sets whether to warn when a font is missing from the calinea config data.
     * In all case when a font is missing, the default character width will be used.
     */
    public CalineaConfig warnOnMissingFonts(boolean warnOnMissingFonts) {
        this.warnOnMissingFonts = warnOnMissingFonts;
        return this;
    }

    /**
     * Sets whether to warn when measuring unresolved SelectorComponents.
     * SelectorComponents must be resolved server-side before being passed to Calinea for accurate measurement.
     * When unresolved, Calinea falls back to measuring the raw selector pattern as text.
     */
    public CalineaConfig warnOnUnresolvedServerComponents(boolean warnOnUnresolvedServerComponents) {
        this.warnOnUnresolvedServerComponents = warnOnUnresolvedServerComponents;
        return this;
    }

    /**
     * Gets whether to warn when measuring unresolved SelectorComponents.
     */
    public boolean warnOnUnresolvedServerComponents() {
        return warnOnUnresolvedServerComponents;
    }

    /**
     * Sets whether to warn when measuring unresolved client-side components.
     * Client-side components must be resolved client-side before being passed to Calinea for accurate measurement.
     * When unresolved, Calinea falls back to measuring the raw component as text.
     */    
    public CalineaConfig warnOnUnforcedClientComponents(boolean warnOnUnforcedClientComponents) {
        this.warnOnUnforcedClientComponents = warnOnUnforcedClientComponents;
        return this;
    }

    public boolean warnOnUnforcedClientComponents() {
        return warnOnUnforcedClientComponents;
    }


    public boolean verboseLogging() {
        return verboseLogging;
    }

    public CalineaConfig verboseLogging(boolean verboseLogging) {
        this.verboseLogging = verboseLogging;
        return this;
    }
}
