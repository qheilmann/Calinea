package io.calinea.pack.reader;

import com.fasterxml.jackson.databind.JsonNode;

import io.calinea.pack.translation.TranslationInfo;
import io.calinea.pack.translation.TranslationsInfo;

import java.io.IOException;

import org.jspecify.annotations.Nullable;

/**
 * Reads the "translations" section from Calinea config JSON.
 * <p>
 * Format:
 * <pre>
 * "translations": [
 *     { "language": "en_us", "entries": { "key": "value", ... } },
 *     { "language": "fr_fr", "entries": { "key": "value", ... } }
 * ]
 * </pre>
 */
public class TranslationsSectionReader implements ISectionReader<TranslationsInfo> {
    
    private static final String SECTION_NAME = "translations";
    
    @Override
    public String getSectionName() {
        return SECTION_NAME;
    }
    
    @Override
    public TranslationsInfo read(JsonNode translationsNode) throws IOException {
        if (!translationsNode.isArray()) {
            throw new IOException("'translations' section must be an array");
        }
        
        TranslationsInfo result = new TranslationsInfo();
        for (JsonNode langNode : translationsNode) {
            result.addTranslation(readSingleLanguage(langNode));
        }
        return result;
    }
    
    /**
     * Reads a single language entry.
     * Format: { "language": "en_us", "entries": { "key": "value", ... } }
     */
    private TranslationInfo readSingleLanguage(JsonNode langNode) throws IOException {
        if (!langNode.isObject()) {
            throw new IOException("Translation entry must be an object");
        }
        
        // Read language (required)
        JsonNode languageNode = langNode.get("language");
        if (languageNode == null || languageNode.asText().isEmpty()) {
            throw new IOException("Missing or invalid 'language' in translation entry");
        }
        String language = languageNode.asText();
        
        TranslationInfo translationInfo = new TranslationInfo(language);
        
        // Read entries
        @Nullable JsonNode entriesNode = langNode.get("entries");
        if (entriesNode != null && entriesNode.isObject()) {
            entriesNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                String value = entry.getValue().asText();
                translationInfo.addTranslation(key, value);
            });
        }
        
        return translationInfo;
    }
}
