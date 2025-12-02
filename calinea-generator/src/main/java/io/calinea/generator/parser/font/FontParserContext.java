package io.calinea.generator.parser.font;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Context information for provider parsing, including paths and resource pack structure.
 */
public class FontParserContext {
    
    private final Path fontDir;
    private final Path resourcePackRoot;
    
    public FontParserContext(Path fontDir) {
        this.fontDir = fontDir;
        this.resourcePackRoot = findResourcePackRoot(fontDir);
    }
    
    /**
     * Gets the directory containing the font definition file.
     */
    public Path getFontDir() {
        return fontDir;
    }
    
    /**
     * Gets the root directory of the resource pack.
     */
    public Path getResourcePackRoot() {
        return resourcePackRoot;
    }
    
    /**
     * Finds the resource pack root by locating pack.mcmeta, walking up from the given directory.
     */
    public static Path findResourcePackRoot(Path startDir) {
        Path current = startDir;
        while (current != null) {
            if (Files.exists(current.resolve("pack.mcmeta"))) {
                return current;
            }
            current = current.getParent();
        }
        
        throw new IllegalStateException("Could not find pack.mcmeta while searching ancestors of: " + startDir);
    }
}
