package io.calinea.generator.parser;

import io.calinea.generator.parser.font.FontParser;
import io.calinea.pack.PackInfo;
import io.calinea.pack.font.FontsInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Parses Minecraft resource packs, coordinating different parsers for fonts, translations, keybinds, etc.
 */
public class MinecraftPackParser {
    
    private final FontParser fontParser;
    // Future: private final TranslationParser translationParser;
    // Future: private final KeybindParser keybindParser;
    
    public MinecraftPackParser() {
        this.fontParser = new FontParser();
    }
    
    /**
     * Parses a resource pack.
     * 
     * @param resourcePackPath the root path of the resource pack
     * @return PackInfo containing all parsed data
     * @throws IOException if parsing fails
     */
    public PackInfo parseResourcePack(Path resourcePackPath) throws IOException {
        validateResourcePack(resourcePackPath);
        
        // Parse fonts
        FontsInfo fonts = fontParser.parse(resourcePackPath);
        fontParser.validate(fonts);
        fontParser.printStatistics(fonts);
        
        // Future: Parse translations
        // TranslationsInfo translations = translationParser.parse(resourcePackPath);
        
        // Future: Parse keybinds  
        // KeybindsInfo keybinds = keybindParser.parse(resourcePackPath);
        
        return new PackInfo(fonts, null, null);
    }
    
    /**
     * Gets the font parser for direct access or configuration.
     */
    public FontParser getFontParser() {
        return fontParser;
    }
    
    /**
     * Validates that the path is a valid resource pack.
     */
    private void validateResourcePack(Path resourcePackPath) throws IOException {
        if (!Files.exists(resourcePackPath)) {
            throw new IOException("Resource pack path does not exist: " + resourcePackPath);
        }
        
        Path packMcmeta = resourcePackPath.resolve("pack.mcmeta");
        if (!Files.exists(packMcmeta)) {
            throw new IOException("Not a valid resource pack (missing pack.mcmeta): " + resourcePackPath);
        }
        
        Path assetsDir = resourcePackPath.resolve("assets");
        if (!Files.exists(assetsDir)) {
            throw new IOException("Assets directory not found: " + assetsDir);
        }
    }

    public void printStatistics(PackInfo packInfo) {
        FontsInfo fonts = packInfo.fontsInfo();
        if (fonts != null) {
            fontParser.printStatistics(fonts);
        }
    }
}