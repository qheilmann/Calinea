package io.calinea;

import java.nio.file.Path;
import java.nio.file.Paths;
import io.calinea.config.CalineaConfig;
import io.calinea.logger.CalineaLogger;

public class CalineaTestHelper {
    public static void setup() {
        Path fontPath = Paths.get("../calinea-output/font-widths.json");
        if (!fontPath.toFile().exists()) {
            // Fallback for when running in some IDE configurations or CI
             fontPath = Paths.get("calinea-output/font-widths.json");
        }
        
        if (!fontPath.toFile().exists()) {
            throw new RuntimeException("Could not find font-widths.json at " + fontPath.toAbsolutePath());
        }

        CalineaConfig config = new CalineaConfig()
            .fontInfoPath(fontPath)
            .logger(CalineaLogger.fromPrintStream(System.out));

        System.out.println("Using font widths from: " + fontPath.toAbsolutePath());
        Calinea.onLoad(config);
    }
}
