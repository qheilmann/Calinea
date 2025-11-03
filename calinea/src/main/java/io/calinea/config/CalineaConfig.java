package io.calinea.config;

import java.nio.file.Path;

public class CalineaConfig {
    private Path fontInfoPath;

    public Path fontInfoPath() {
        return fontInfoPath;
    }

    public CalineaConfig fontInfoPath(Path fontInfoPath) {
        this.fontInfoPath = fontInfoPath;
        return this;
    }
}
