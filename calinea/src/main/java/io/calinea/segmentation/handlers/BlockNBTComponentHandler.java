package io.calinea.segmentation.handlers;

import io.calinea.Calinea;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.TextComponent;

/**
 * Handler for BlockNBTComponent.
 * <p>
 * BlockNBTComponents are supposed to be resolved server-side. If not, they will not be resolved 
 * client-side either and will show an empty TextComponent instead.
 */
public class BlockNBTComponentHandler implements IComponentLayoutHandler<BlockNBTComponent>{

    public BlockNBTComponentHandler() {}

    @Override
    public boolean canHandle(Component component) {
        return component instanceof BlockNBTComponent;
    }

    @Override
    public double measureRoot(BlockNBTComponent component) {

        // Warn that an unresolved BlockNBTComponent is being measured
        if (Calinea.getConfig().warnOnUnresolvedServerComponents()) {
            Calinea.getLogger().warning(String.format(
                "Unresolved BlockNBTComponent detected - position:\"%s\". " +
                "It should be resolved server-side before measurement. " +
                "Falling back to an empty component. " +
                "This may indicate that the component was not properly resolved before using the %s API.",
                component.pos(), Calinea.LIBRARY_NAME));
        }

        return 0;
    }

    @Override
    public boolean isAtomic() {
        return true;
    }

    @Override
    public TextComponent asTextComponent(BlockNBTComponent component) {
        return Component.empty().style(component.style());
    }
}
