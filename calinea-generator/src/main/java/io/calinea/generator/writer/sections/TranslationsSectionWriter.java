package io.calinea.generator.writer.sections;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.calinea.pack.translation.TranslationInfo;
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
 *     {
 *         "language": "en_us",
 *         "entries": {
 *             "item.minecraft.wind_charge": "Wind Charge",
 *             "entity.minecraft.pig": "Pig"
 *         }
 *     },
 *     {
 *         "language": "fr_fr",
 *         "entries": {
 *             "entity.minecraft.pig": "Cochon"
 *         }
 *     }
 * ]
 * </pre>
 */
public class TranslationsSectionWriter implements ISectionWriter {
    
    private final TranslationsInfo translationsInfo;
    
    /**
     * Creates a new TranslationsSectionWriter.
     * 
     * @param translations the translations info containing all languages
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
        
        ArrayNode translationsArray = root.putArray("translations");
        
        for (TranslationInfo langInfo : translationsInfo.getLanguages().values()) {
            ObjectNode langNode = translationsArray.addObject();
            langNode.put("language", langInfo.language());
            
            ObjectNode entriesObject = langNode.putObject("entries");
            for (Map.Entry<String, String> entry : langInfo.translations().entrySet()) {
                entriesObject.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    @Override
    public void printStatistics() {
        if (hasData()) {
            System.out.println("  - " + translationsInfo.totalEntryCount() + " translations across " 
                + translationsInfo.languageCount() + " language(s)");
            for (TranslationInfo lang : translationsInfo.getLanguages().values()) {
                System.out.println("    - " + lang.language() + ": " + lang.size() + " entries");
            }
        }
    }
}
