package io.calinea.resolver.Client;

import java.util.List;
import java.util.Locale;

import io.calinea.pack.PackInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

public class ForcedClientComponentResolver {

    private static List<IClientComponentResolver<?>> clientResolvers;

    public ForcedClientComponentResolver(PackInfo packInfo) {
        clientResolvers = List.of(
                new KeyBindComponentResolver(),
                new TranslatableComponentResolver(packInfo)
        );
    }

    // public Component resolve(ComponentLike component) {
    //    return resolve(component, null);
    // }

    public Component resolve(ComponentLike componentLike, Locale locale) {
        if (componentLike == Component.empty()) {
            return Component.empty();
        }
        
        Component component = componentLike.asComponent();

        // Resolve if client-side else skip
        for (IClientComponentResolver<?> resolver : clientResolvers) {

            if (resolver.canResolve(component)) {
                // Resolve root
                @SuppressWarnings("unchecked")
                IClientComponentResolver<Component> typedResolver = (IClientComponentResolver<Component>) resolver;
                component = typedResolver.resolve(component, locale);
            }
        }
                
        // Resolve children
        List<Component> resolvedChildren = component.children().stream()
            .map(child -> resolve(child, locale))
            .toList();
        
        return component.children(resolvedChildren);
    }
}

// idk if a take the children recursively
// or separately like measurer


// public double measure(ComponentLike componentLike, @Nullable Style parentStyle) {
//         Component component = componentLike.asComponent();
        
//         // Apply parent style to current component for measurement
//         if (parentStyle == null) {
//             parentStyle = Style.empty();
//         }
//         Style componentStyle = component.style();
//         Style mergedStyle = componentStyle.merge(parentStyle, Merge.Strategy.IF_ABSENT_ON_TARGET, Merge.DECORATIONS);
//         component = component.style(mergedStyle);
        
//         // Find the appropriate measurer for this component type
//         for (IComponentMeasurer<?> measurer : measurers) {
//             if (measurer.canHandle(component)) {
//                 @SuppressWarnings("unchecked")
//                 IComponentMeasurer<Component> typedMeasurer = (IComponentMeasurer<Component>) measurer;
//                 return typedMeasurer.measureRoot(component) + measureChildren(component);
//             }
//         }
        
//         throw new UnsupportedOperationException(
//             "No measurer found for component type: " + component.getClass().getSimpleName());
//     }

//     private double measureChildren(ComponentLike componentLike) {
//         double totalChildWidth = 0;
//         Component component = componentLike.asComponent();

//         for (ComponentLike child : component.children()) {
//             totalChildWidth += measure(child, component.style());
//         }
//         return totalChildWidth;
//     }