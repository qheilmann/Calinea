package io.calinea.pack;

import io.calinea.pack.font.FontsInfo;
import io.calinea.pack.keybind.KeybindsInfo;
import io.calinea.pack.translation.TranslationsInfo;

/**
 * Contains all parsed data from a Calinea config file.
 */
public class PackInfo {
    
    private final FontsInfo fonts;
    private final KeybindsInfo keybinds;
    private final TranslationsInfo translations;

    public PackInfo(
            FontsInfo fonts,
            KeybindsInfo keybinds,
            TranslationsInfo translations) {
        this.fonts = fonts;
        this.keybinds = keybinds;
        this.translations = translations;
    }

    public FontsInfo fontsInfo() {
        return fonts;
    }
    
    public KeybindsInfo keybindsInfo() {
        return keybinds;
    }
    
    public TranslationsInfo translationsInfo() {
        return translations;
    }
}
