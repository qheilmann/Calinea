package io.calinea.segmentation.measurer;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.calinea.segmentation.handlers.BlockNBTComponentHandler;
import io.calinea.segmentation.handlers.EntityNBTComponentHandler;
import io.calinea.segmentation.handlers.IComponentLayoutHandler;
import io.calinea.segmentation.handlers.KeybindComponentHandler;
import io.calinea.segmentation.handlers.ObjectComponentHandler;
import io.calinea.segmentation.handlers.ScoreComponentHandler;
import io.calinea.segmentation.handlers.SelectorComponentHandler;
import io.calinea.segmentation.handlers.StorageNBTComponentHandler;
import io.calinea.segmentation.handlers.TextComponentHandler;
import io.calinea.segmentation.handlers.TranslatableComponentHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.Style.Merge;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Main ComponentMeasurer that delegates to specific component measurers
 * based on the component type using polymorphism.
 */
public class ComponentMeasurer implements IComponentMeasurer{
    private final List<IComponentLayoutHandler<?>> measurers;

    private TextComponentHandler textComponentMeasurer;

    public ComponentMeasurer(ComponentMeasurerConfig config) {
        this.textComponentMeasurer = new TextComponentHandler(config);
        this.measurers = List.of(
            textComponentMeasurer,
            new SelectorComponentHandler(config),
            new ScoreComponentHandler(),
            new ObjectComponentHandler(config),
            new BlockNBTComponentHandler(),
            new EntityNBTComponentHandler(),
            new StorageNBTComponentHandler(),
            new KeybindComponentHandler(config),
            new TranslatableComponentHandler(config)
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
        
        double rootWidth = measureRoot(component);
        double childrenWidth = measureChildren(component);

        return rootWidth + childrenWidth;
    }

    private double measureChildren(ComponentLike componentLike) {
        double totalChildWidth = 0;
        Component component = componentLike.asComponent();

        for (ComponentLike child : component.children()) {
            totalChildWidth += measure(child, component.style());
        }
        return totalChildWidth;
    }

    public double measureRoot(ComponentLike componentLike) {
        Component component = componentLike.asComponent();

        // Find the appropriate measurer for this component type
        for (IComponentLayoutHandler<?> measurer : measurers) {
            if (measurer.canHandle(component)) {
                @SuppressWarnings("unchecked")
                IComponentLayoutHandler<Component> typedMeasurer = (IComponentLayoutHandler<Component>) measurer;
                return typedMeasurer.measureRoot(component);
            }
        }

        throw new UnsupportedOperationException(
            "No measurer found for component type: " + component.getClass().getSimpleName());
    }

    /**
     * Measures the width of a plain text string with the given style.
     * This is a helper method to avoid creating TextComponent objects just for measurement.
     */
    public double measureText(String text, Style style) {
        return textComponentMeasurer.measureTextWidth(text, style.font(), style.hasDecoration(TextDecoration.BOLD));
    }

    /**
     * Converts a component to its TextComponent representation for splitting.
     * Delegates to the appropriate IComponentMeasurer.
     */
    public @Nullable TextComponent asTextComponent(Component component) {
        for (IComponentLayoutHandler<?> measurer : measurers) {
            if (measurer.canHandle(component)) {
                @SuppressWarnings("unchecked")
                IComponentLayoutHandler<Component> typedMeasurer = (IComponentLayoutHandler<Component>) measurer;
                return typedMeasurer.asTextComponent(component);
            }
        }
        return null;
    }

    /**
     * Checks if the component is atomic (cannot be split).
     */
    public boolean isAtomic(Component component) {
        for (IComponentLayoutHandler<?> measurer : measurers) {
            if (measurer.canHandle(component)) {
                return measurer.isAtomic();
            }
        }
        // Default to atomic if unknown
        return true;
    }
}