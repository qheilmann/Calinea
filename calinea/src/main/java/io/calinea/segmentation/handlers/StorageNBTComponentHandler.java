package io.calinea.segmentation.handlers;

import io.calinea.Calinea;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.StorageNBTComponent;

/**
 * Handler for StorageNBTComponent.
 * <p>
 * StorageNBTComponents are supposed to be resolved server-side. If not, they will not be resolved 
 * client-side either and will show an empty TextComponent instead.
 */
public class StorageNBTComponentHandler implements IComponentLayoutHandler<StorageNBTComponent>{

    public StorageNBTComponentHandler() {}

    @Override
    public boolean canHandle(Component component) {
        return component instanceof StorageNBTComponent;
    }

    @Override
    public double measureRoot(StorageNBTComponent component) {

        // Warn that an unresolved StorageNBTComponent is being measured
        if (Calinea.config().warnOnUnresolvedServerComponents()) {
            Calinea.logger().warning(String.format(
                "Unresolved StorageNBTComponent detected - '%s'. " +
                "It should be resolved server-side before measurement. " +
                "Falling back to an empty component. " +
                "This may indicate that the component was not properly resolved before using the %s API.",
                component.storage(), Calinea.LIBRARY_NAME));
        }

        return 0;
    }

    @Override
    public boolean isAtomic() {
        return true;
    }

    @Override
    public TextComponent asTextComponent(StorageNBTComponent component) {
        return Component.empty().style(component.style());
    }
}
