package io.calinea.config;

import java.nio.file.Path;

import io.calinea.logger.CalineaLogger;

public class CalineaConfig {
    private Path fontInfoPath = Path.of("./font-widths.json");
    private CalineaLogger logger = CalineaLogger.silent();
    private Path fontInfoPath;

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
}
