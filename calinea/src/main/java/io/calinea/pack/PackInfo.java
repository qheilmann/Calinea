package io.calinea.pack;

import io.calinea.pack.font.FontsInfo;
import io.calinea.pack.translation.TranslationsInfo;

/**
 * Contains all parsed data from a Calinea config file.
 */
public class PackInfo {
    
    private final FontsInfo fonts;
    private final TranslationsInfo translations;

    public PackInfo(
            FontsInfo fonts,
            TranslationsInfo translations) {
        this.fonts = fonts;
        this.translations = translations;
    }

    public FontsInfo fontsInfo() {
        return fonts;
    }
    
    public TranslationsInfo translationsInfo() {
        return translations;
    }
}
