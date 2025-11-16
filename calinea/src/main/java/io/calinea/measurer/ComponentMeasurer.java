package io.calinea.measurer;

import java.util.List;

import edu.umd.cs.findbugs.annotations.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.Style.Merge;

/**
 * Main ComponentMeasurer that delegates to specific component measurers
 * based on the component type using polymorphism.
 */
public class ComponentMeasurer {
    private final ComponentMeasurerConfig config;
    private final List<IComponentMeasurer<?>> measurers;

    public ComponentMeasurer(ComponentMeasurerConfig config) {
        this.config = config;
        this.measurers = List.of(
            new TextComponentMeasurer(config),
            new SelectorComponentMeasurer(config)
            // TODO
            // Add other measurer implementations here as you create them
            // new TranslatableComponentMeasurer(config),
            // new KeybindComponentMeasurer(config),
            // etc.
            // All serverSide component must be resolved before here, and if not resolve use a bad fallback and warn
            // All clientSide component can be resolved before here, and if not resolve use the right fallback and maybe warn
        );
    }

    public double measure(ComponentLike componentLike) {
        return measure(componentLike, null);
    }

    // ["P":red ["A" ["B:blue" "C"]]]
    // measure(["P":red ["A" ["B:blue" "C"]]], null))
    // apply parent style null
    // = measureRoot("P":red + skipedChild) + measureChildren(["P":red ["A" ["B:blue" "C"]]])

    // measureChildren() >
    // measure(["A" ["B:blue" "C"]], red)
    
    // measure() >
    // apply parent style red
    // = measureRoot("A" merged red + skipedChild) + measureChildren(["A":red ["B:blue" "C"]])

    // measureChildren() >
    // measure(["B:blue" "C"], red)

    // measure() >
    // apply parent style red but "B:blue" have blue, so merged is blue
    // = measureRoot("B:blue" + skipedChild) + measureChildren(["B:blue" "C"]

    // measureChildren() >
    // measure("C", blue)

    // measure() >
    // apply parent style blue
    // = measureRoot("C" merged blue + skipedChild) + measureChildren(["C":blue])

    // measureChildren() >
    // no children, return 0

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