package io.calinea.generator.writer.sections;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.calinea.pack.translation.TranslationsInfo;

import java.util.Map;

/**
 * Writes the translations section containing translation key-to-text mappings.
 * <p>
 * Translations map Minecraft translation keys (like "entity.minecraft.pig") 
 * to their display text for measuring purposes.
 * <p>
 * Output format:
 * <pre>
 * "translations": [
    * {
    *     "language": "en_us",
    *     "entries": [
    *         {"item.minecraft.wind_charge": "Wind Charge"},
    *         {"entity.minecraft.pig": "Pig"}
    *     ]
    * }
 * ]
 * </pre>
 */
public class TranslationsSectionWriter implements ISectionWriter {
    
    private final TranslationsInfo translationsInfo;
    
    /**
     * Creates a new TranslationsSectionWriter.
     * 
     * @param translations map of translation keys to their display text
     * @param language the language code (e.g., "en_us", "fr_fr")
     */
    public TranslationsSectionWriter(TranslationsInfo translations) {
        this.translationsInfo = translations;
    }
    
    @Override
    public String getSectionName() {
        return "translations";
    }
    
    @Override
    public boolean hasData() {
        return translationsInfo != null && !translationsInfo.isEmpty();
    }
    
    @Override
    public void writeSection(ObjectNode root) {
        if (!hasData()) {
            return;
        }
        
        ObjectNode translationsNode = root.putObject("translations");
        translationsNode.put("language", translationsInfo.language());

        ObjectNode entriesObject = translationsNode.putObject("entries");

        for (Map.Entry<String, String> entry : translationsInfo.translations().entrySet()) {
            entriesObject.put(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public void printStatistics() {
        if (hasData()) {
            System.out.println("  - " + translationsInfo.size() + " translations (" + translationsInfo.language() + ")");
        }
    }
}
