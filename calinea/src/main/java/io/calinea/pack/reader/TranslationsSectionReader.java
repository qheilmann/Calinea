package io.calinea.pack.reader;

import com.fasterxml.jackson.databind.JsonNode;

import io.calinea.pack.translation.TranslationsInfo;

import java.io.IOException;

import org.jspecify.annotations.Nullable;

/**
 * Reads the "translations" section from Calinea config JSON.
 * Format: { "language": "en_us", "entries": { "key": "value", ... } }
 */
public class TranslationsSectionReader implements ISectionReader<TranslationsInfo> {
    
    private static final String SECTION_NAME = "translations";
    
    @Override
    public String getSectionName() {
        return SECTION_NAME;
    }
    
    @Override
    public TranslationsInfo read(JsonNode translationsNode) throws IOException {
        if (!translationsNode.isObject()) {
            throw new IOException("'translations' section must be an object");
        }
        
        // Read language (required)
        JsonNode languageNode = translationsNode.get("language");
        if (languageNode == null || languageNode.asText().isEmpty()) {
            throw new IOException("Missing or invalid 'language' in translations section");
        }
        String language = languageNode.asText();
        
        TranslationsInfo translationsInfo = new TranslationsInfo(language);
        
        // Read entries
        @Nullable JsonNode entriesNode = translationsNode.get("entries");
        if (entriesNode != null && entriesNode.isObject()) {
            entriesNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                String value = entry.getValue().asText();
                translationsInfo.addTranslation(key, value);
            });
        }
        
        return translationsInfo;
    }
}
