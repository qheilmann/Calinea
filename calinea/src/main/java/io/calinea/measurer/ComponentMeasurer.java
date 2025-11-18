package io.calinea.measurer;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.calinea.measurer.components.ObjectComponentMeasurer;
import io.calinea.measurer.components.EntityNBTComponentMeasurer;
import io.calinea.measurer.components.KeybindComponentMeasurer;
import io.calinea.measurer.components.ScoreComponentMeasurer;
import io.calinea.measurer.components.SelectorComponentMeasurer;
import io.calinea.measurer.components.StorageNBTComponentMeasurer;
import io.calinea.measurer.components.TextComponentMeasurer;
import io.calinea.measurer.components.TranslatableComponentMeasurer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.Style.Merge;

/**
 * Main ComponentMeasurer that delegates to specific component measurers
 * based on the component type using polymorphism.
 */
public class ComponentMeasurer {
    private final List<IComponentMeasurer<?>> measurers;

    public ComponentMeasurer(ComponentMeasurerConfig config) {
        this.measurers = List.of(
            new TextComponentMeasurer(config),
            new SelectorComponentMeasurer(config),
            new ScoreComponentMeasurer(),
            new ObjectComponentMeasurer(config),
            new EntityNBTComponentMeasurer(),
            new StorageNBTComponentMeasurer(),
            new KeybindComponentMeasurer(config),
            new TranslatableComponentMeasurer(config)
        );
    }

    public double measure(ComponentLike componentLike) {
        return measure(componentLike, null);
    }

    public double measure(ComponentLike componentLike, @Nullable Style parentStyle) {
        Component component = componentLike.asComponent();
        
        // Apply parent style to current component for measurement
        if (parentStyle == null) {
            parentStyle = Style.empty();
        }
        Style componentStyle = component.style();
        Style mergedStyle = componentStyle.merge(parentStyle, Merge.Strategy.IF_ABSENT_ON_TARGET, Merge.DECORATIONS);
        component = component.style(mergedStyle);
        
        // Find the appropriate measurer for this component type
        for (IComponentMeasurer<?> measurer : measurers) {
            if (measurer.canHandle(component)) {
                @SuppressWarnings("unchecked")
                IComponentMeasurer<Component> typedMeasurer = (IComponentMeasurer<Component>) measurer;
                return typedMeasurer.measureRoot(component) + measureChildren(component);
            }
        }
        
        throw new UnsupportedOperationException(
            "No measurer found for component type: " + component.getClass().getSimpleName());
    }

    private double measureChildren(ComponentLike componentLike) {
        double totalChildWidth = 0;
        Component component = componentLike.asComponent();

        for (ComponentLike child : component.children()) {
            totalChildWidth += measure(child, component.style());
        }
        return totalChildWidth;
    }
}