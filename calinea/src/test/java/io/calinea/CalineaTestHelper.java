package io.calinea;

import java.nio.file.Path;
import java.nio.file.Paths;
import io.calinea.config.CalineaConfig;
import io.calinea.config.CalineaGeneratorDefault;
import io.calinea.logger.CalineaLogger;

public class CalineaTestHelper {
    public static void setup() {
        Path calineaConfigPath = Paths.get("..", CalineaGeneratorDefault.DEFAULT_OUTPUT_DIR, CalineaGeneratorDefault.DEFAULT_OUTPUT_FILENAME);
        if (!calineaConfigPath.toFile().exists()) {
            // Fallback for when running in some IDE configurations or CI
             calineaConfigPath = Paths.get(CalineaGeneratorDefault.DEFAULT_OUTPUT_DIR, CalineaGeneratorDefault.DEFAULT_OUTPUT_FILENAME);
        }
        
        if (!calineaConfigPath.toFile().exists()) {
            throw new RuntimeException("Could not find " + CalineaGeneratorDefault.DEFAULT_OUTPUT_FILENAME + " at " + calineaConfigPath.toAbsolutePath());
        }

        CalineaConfig config = new CalineaConfig()
            .calineaConfigPath(calineaConfigPath)
            .logger(CalineaLogger.fromPrintStream(System.out));

        System.out.println("Using config from: " + calineaConfigPath.toAbsolutePath());
        Calinea.onLoad(config);
    }
}
