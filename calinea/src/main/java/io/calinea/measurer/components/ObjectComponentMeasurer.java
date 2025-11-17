package io.calinea.measurer.components;

import io.calinea.Calinea;
import io.calinea.measurer.ComponentMeasurerConfig;
import io.calinea.measurer.IComponentMeasurer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;

public class ObjectComponentMeasurer implements IComponentMeasurer<ObjectComponent>{
    ComponentMeasurerConfig config;

    public ObjectComponentMeasurer(ComponentMeasurerConfig config) {
        this.config = config;
    }

    @Override
    public boolean canHandle(Component component) {
        return component instanceof ObjectComponent;
    }

    @Override
    public double measureRoot(ObjectComponent component) {

        // WAITING FOR DECISION 1.21.9+
        
        // TODO
        // hmm this is resolved by client but always 8x8 so this warning are useless ? to be tested on 1.21.9+
        // with reel atlas and with unresolved key (fail server or only on client)

        // // ObjectComponent are supposed to be resolved server-side, if not it will not be resolved client-side either and will show an empty TextComponent instead

        // // Warn that an unresolved ObjectComponent is being measured
        // if (Calinea.getConfig().warnOnUnresolvedServerComponents()) {
        //     ObjectContents contents = component.contents();
        //     if (contents instanceof PlayerHeadObjectContents playerHeadContents) {
        //         // Try to get the name in order of preference: name -> id -> first profile property -> "unknown"
        //         String identifier = playerHeadContents.name() != null ? playerHeadContents.name() :
        //                playerHeadContents.id() != null ? playerHeadContents.id().toString() :
        //                !playerHeadContents.profileProperties().isEmpty() ? playerHeadContents.profileProperties().getFirst().value() :
        //                "unknown";
        //         Calinea.getLogger().warning(String.format(
        //             "Unresolved ObjectComponent with PlayerHeadObjectContents detected - '%s'. " +
        //             "It should be resolved server-side before measurement. " +
        //             "Falling back to an empty component. " +
        //             "This may indicate that the component was not properly resolved before using the %s API.",
        //             identifier, Calinea.LIBRARY_NAME));
        //     }
        //     else if (contents instanceof SpriteObjectContents spriteContents) {
        //         String atlasAndKey = spriteContents.atlas() + " on " + spriteContents.sprite();
        //         Calinea.getLogger().warning(String.format(
        //             "Unresolved ObjectComponent with SpriteObjectContents detected - '%s'. " +
        //             "It should be resolved server-side before measurement. " +
        //             "Falling back to an empty component. " +
        //             "This may indicate that the component was not properly resolved before using the %s API.",
        //             atlasAndKey, Calinea.LIBRARY_NAME));
        //     }
        //     else {
        //         throw new IllegalStateException("Unknown ObjectContents type: " + contents.getClass().getName());
        //     }
        // }

        return 0;
    }
}
