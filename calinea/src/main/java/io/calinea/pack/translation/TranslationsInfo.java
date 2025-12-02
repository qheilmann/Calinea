package io.calinea.pack.translation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

/**
 * Contains translation mappings from translation keys to their localized strings.
 * For example: "entity.minecraft.pig" -> "Pig"
 */
public class TranslationsInfo {
    
    private static final TranslationsInfo EMPTY = new TranslationsInfo("");
    
    /**
     * Returns an empty TranslationsInfo instance.
     */
    public static TranslationsInfo empty() {
        return EMPTY;
    }
    
    private final String language;
    private final Map<String, String> translations;
    
    public TranslationsInfo(String language) {
        this.language = language;
        this.translations = new LinkedHashMap<>();
    }
    
    public TranslationsInfo(String language, Map<String, String> translations) {
        this.language = language;
        this.translations = new LinkedHashMap<>(translations);
    }
    
    public String language() {
        return language;
    }
    
    public void addTranslation(String key, String value) {
        translations.put(key, value);
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
        return String.format("TranslationsInfo{language='%s', count=%d}", language, translations.size());
    }
}
