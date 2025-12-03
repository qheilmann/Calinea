package io.calinea.pack.translation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedCollection;

import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

/**
 * Container for all translation information across multiple languages.
 * Each language has its own {@link TranslationInfo} with merged translations
 */
public class TranslationsInfo {
    
    private final Map<String, TranslationInfo> languages;
    
    public TranslationsInfo() {
        this.languages = new LinkedHashMap<>();
    }
    
    public TranslationsInfo(SequencedCollection<TranslationInfo> translations) {
        this.languages = new LinkedHashMap<>();
        for (TranslationInfo translation : translations) {
            this.languages.put(translation.language(), translation);
        }
    }
    
    /**
     * Adds or merges a TranslationInfo for a language.
     * If the language already exists, the new translations are merged (later wins).
     */
    public TranslationsInfo addTranslation(TranslationInfo translationInfo) {
        String lang = translationInfo.language();
        TranslationInfo existing = languages.get(lang);
        if (existing != null) {
            existing.merge(translationInfo);
        } else {
            languages.put(lang, translationInfo);
        }
        return this;
    }
    
    /**
     * Adds all TranslationInfo entries, merging if languages already exist.
     */
    public TranslationsInfo addAll(SequencedCollection<TranslationInfo> translations) {
        for (TranslationInfo translation : translations) {
            addTranslation(translation);
        }
        return this;
    }
    
    /**
     * Gets the TranslationInfo for a specific language.
     */
    public @Nullable TranslationInfo getTranslation(String language) {
        return languages.get(language);
    }
    
    /**
     * Gets all languages available.
     */
    @Unmodifiable
    public Map<String, TranslationInfo> getLanguages() {
        return Collections.unmodifiableMap(languages);
    }
    
    /**
     * Convenience method to get a translation for a specific key in a specific language.
     */
    public @Nullable String getTranslation(String language, String key) {
        TranslationInfo info = languages.get(language);
        return info != null ? info.getTranslation(key) : null;
    }
    
    public boolean isEmpty() {
        return languages.isEmpty();
    }
    
    /**
     * Returns the total number of languages.
     */
    public int languageCount() {
        return languages.size();
    }
    
    /**
     * Returns the total number of translation entries across all languages.
     */
    public int totalEntryCount() {
        return languages.values().stream()
            .mapToInt(TranslationInfo::size)
            .sum();
    }
    
    @Override
    public String toString() {
        return String.format("TranslationsInfo{languages=%d, totalEntries=%d}", 
            languageCount(), totalEntryCount());
    }
}
