package io.calinea.resolver.Client;

import io.calinea.Calinea;
import io.calinea.utils.TranslatableComponentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
public class TranslatableComponentResolver implements IClientComponentResolver<TranslatableComponent> {
    
    @Override
    public boolean canResolve(Component component) {
        return component instanceof TranslatableComponent;
    }

    @Override
    public Component resolve(TranslatableComponent translatableComponent) {

        TextComponent result = TranslatableComponentUtils.flattenInEnglish(translatableComponent);

        if (Calinea.config().warnOnUnforcedClientComponents()) {
            String translationPattern = TranslatableComponentUtils.extractEnglishTranslation(translatableComponent);
            Calinea.logger().info("TranslatableComponent resolution is not fully supported yet, and can't be customized, it will be replaced with the english fallback: " + translationPattern);
        }

        return result;
    }
}