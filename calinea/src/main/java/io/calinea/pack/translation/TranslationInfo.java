package io.calinea.pack.translation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

/**
 * Contains translation mappings for a single language.
 * For example: "entity.minecraft.pig" -> "Pig"
 */
public class TranslationInfo {
    
    private final String language;
    private final Map<String, String> translations;
    
    public TranslationInfo(String language) {
        this.language = language;
        this.translations = new LinkedHashMap<>();
    }
    
    public TranslationInfo(String language, Map<String, String> translations) {
        this.language = language;
        this.translations = new LinkedHashMap<>(translations);
    }
    
    public String language() {
        return language;
    }
    
    public void addTranslation(String key, String value) {
        translations.put(key, value);
    }
    
    /**
     * Merges translations from another TranslationInfo into this one.
     * Later values overwrite earlier ones (last wins).
     */
    public void merge(TranslationInfo other) {
        if (!this.language.equals(other.language)) {
            throw new IllegalArgumentException(
                "Cannot merge translations with different languages: " + this.language + " vs " + other.language);
        }
        this.translations.putAll(other.translations);
    }
    
    /**
     * Merges all entries from a map into this TranslationInfo.
     * Later values overwrite earlier ones (last wins).
     */
    public void mergeAll(Map<String, String> entries) {
        this.translations.putAll(entries);
    }
    
    public @Nullable String getTranslation(String key) {
        return translations.get(key);
    }
    
    @Unmodifiable
    public Map<String, String> translations() {
        return Collections.unmodifiableMap(translations);
    }
    
    public boolean isEmpty() {
        return translations.isEmpty();
    }
    
    public int size() {
        return translations.size();
    }
    
    @Override
    public String toString() {
        return String.format("TranslationInfo{language='%s', count=%d}", language, translations.size());
    }
}
