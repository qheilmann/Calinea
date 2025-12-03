package io.calinea.resolver.Client;

import java.util.Locale;

import org.jspecify.annotations.Nullable;

import io.calinea.Calinea;
import io.calinea.pack.PackInfo;
import io.calinea.pack.translation.TranslationInfo;
import io.calinea.pack.translation.TranslationsInfo;
import io.calinea.resolver.ComponentResolver;
import io.calinea.utils.TranslatableComponentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

public class TranslatableComponentResolver implements IClientComponentResolver<TranslatableComponent> {
    
    private final PackInfo packInfo;

    public TranslatableComponentResolver(PackInfo packInfo) {
        this.packInfo = packInfo;
    }

    @Override
    public boolean canResolve(Component component) {
        return component instanceof TranslatableComponent;
    }

    /**
     * Resolution priority:
     * <pre>
     * locale | en_us | key in locale | key in en_us | result
     * -------|-------|---------------|--------------|------------------------------------------
     *   1    |   -   |       1       |      -       | translation from locale (no warning)
     *   1    |   1   |       0       |      1       | translation from en_us (warn: missing key in locale)
     *   1    |   -   |       0       |      0       | flatten english (warn: no translation data)
     *   0    |   1   |       -       |      1       | translation from en_us (warn: missing locale)
     *   0    |   1   |       -       |      0       | flatten english (warn: no translation data)
     *   0    |   0   |       -       |      -       | flatten english (warn: no translation data)
     * </pre>
     */
    @Override
    public Component resolve(TranslatableComponent component, Locale locale) {
        String localeStr = locale.toString().toLowerCase();
        String key = component.key();
        TranslationsInfo translations = packInfo.translationsInfo();
        
        // Try locale first, then fallback to en_us
        @Nullable String translation = tryResolve(translations, localeStr, key);
        if (translation != null) {
            return buildComponent(component, translation);
        }
        
        // Locale missing or key not found - try en_us fallback
        boolean hasLocale = translations.getTranslation(localeStr) != null;
        translation = tryResolve(translations, ComponentResolver.MINECRAFT_FALLBACK_LOCAL, key);
        
        if (translation != null) {
            warnMissingButFallbackFound(localeStr, key, hasLocale);
            return buildComponent(component, translation);
        }
        
        // No translation found anywhere - use hardcoded english flatten
        warnNoTranslation(localeStr, key);
        return TranslatableComponentUtils.flattenInEnglish(component);
    }
    
    /**
     * Tries to resolve a translation key in a specific locale.
     * @return the translation string, or null if not found
     */
    private @Nullable String tryResolve(TranslationsInfo translations, String localeStr, String key) {
        TranslationInfo info = translations.getTranslation(localeStr);
        return info != null ? info.getTranslation(key) : null;
    }
    
    /**
     * Builds a Component from a translation pattern and the original component's arguments/style.
     */
    private Component buildComponent(TranslatableComponent original, String translation) {
        return TranslatableComponentUtils.buildFromPattern(original, translation);
    }
    
    private void warnMissingButFallbackFound(String locale, String key, boolean hasLocale) {
        if (!Calinea.config().warnOnUnforcedClientComponents()) return;
        
        if (hasLocale) {
            Calinea.logger().warning("Translation key '" + key + "' not found for locale '" + locale + "', using en_us fallback");
        } else {
            Calinea.logger().warning("Locale '" + locale + "' not found, using en_us fallback for key '" + key + "'");
        }
    }
    
    private void warnNoTranslation(String locale, String key) {
        if (!Calinea.config().warnOnUnforcedClientComponents()) return;

        Calinea.logger().warning("No translation data available, using TranslationComponent fallback or hardcoded english for key '" + key + "'");
    }
}