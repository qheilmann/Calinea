package io.calinea.generator.parser.translation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.calinea.generator.parser.IResourceParser;
import io.calinea.pack.translation.TranslationInfo;
import io.calinea.pack.translation.TranslationsInfo;

/**
 * Parses translation files from a Minecraft resource pack.
 * <p>
 * Scans all namespace directories under assets/ for lang/*.json files.
 * Conflicting translations are merged following filesystem order,
 * where later entries overwrite earlier ones (last wins).
 * <p>
 * Structure: assets/{namespace}/lang/{language}.json
 * Example: assets/minecraft/lang/en_us.json
 */
public class TranslationParser implements IResourceParser<TranslationsInfo> {
    
    private static final String ASSETS_DIRECTORY = "assets";
    private static final String LANG_SUBDIRECTORY = "lang";
    private static final String JSON_EXTENSION = ".json";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public TranslationsInfo parse(Path resourcePackRoot) {
        TranslationsInfo result = new TranslationsInfo();
        
        Path assetsDir = resourcePackRoot.resolve(ASSETS_DIRECTORY);
        if (!Files.isDirectory(assetsDir)) {
            System.err.println("Assets directory not found: " + assetsDir);
            return result;
        }
        
        try {
            // Iterate namespaces in filesystem order
            // Later namespaces overwrite earlier ones for the same translation keys
            for (Path namespaceDir : Files.list(assetsDir).filter(Files::isDirectory).toList()) {
                Path langDir = namespaceDir.resolve(LANG_SUBDIRECTORY);
                // Parse all language files in this namespace
                parseLanguageDirectory(langDir, result);
            }
        } catch (IOException e) {
            System.err.println("Failed to scan assets directory: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Parses all language JSON files in a lang directory.
     */
    private void parseLanguageDirectory(Path langDir, TranslationsInfo result) {
        try {
            for (Path langFile : Files.list(langDir)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(JSON_EXTENSION))
                    .toList()) {
                
                String language = extractLanguageCode(langFile);
                parseLanguageFile(langFile, language, result);
            }
        } catch (IOException e) {
            System.err.println("Failed to scan lang directory " + langDir + ": " + e.getMessage());
        }
    }
    
    /**
     * Parses a single language JSON file and merges its translations.
     */
    private void parseLanguageFile(Path langFile, String language, TranslationsInfo result) {
        try {
            Map<String, String> translations = objectMapper.readValue(
                langFile.toFile(), 
                new TypeReference<Map<String, String>>() {}
            );
            
            // Get or create TranslationInfo for this language
            TranslationInfo langInfo = result.getTranslation(language);
            if (langInfo == null) {
                langInfo = new TranslationInfo(language);
                result.addTranslation(langInfo);
            }
            
            // Merge translations (later files overwrite earlier - last wins)
            langInfo.mergeAll(translations);
            
        } catch (IOException e) {
            System.err.println("Failed to parse language file " + langFile + ": " + e.getMessage());
        }
    }
    
    /**
     * Extracts the lowercased language code from a file path.
     * Example: en_us.json -> en_us
     */
    private String extractLanguageCode(Path langFile) {
        String filename = langFile.getFileName().toString();
        return filename.substring(0, filename.length() - JSON_EXTENSION.length()).toLowerCase();
    }

    @Override
    public void validate(TranslationsInfo result) {
        // Check for empty languages
        for (TranslationInfo lang : result.getLanguages().values()) {
            if (lang.isEmpty()) {
                System.out.println("Warning: Language '" + lang.language() + "' has no translations");
            }
        }
    }

    @Override
    public void printStatistics(TranslationsInfo result) {
        System.out.println("Translations:");
        System.out.println("  - " + result.languageCount() + " language(s)");
        System.out.println("  - " + result.totalEntryCount() + " total entries");
        
        for (TranslationInfo lang : result.getLanguages().values()) {
            System.out.println("    - " + lang.language() + ": " + lang.size() + " entries");
        }
    }
}
