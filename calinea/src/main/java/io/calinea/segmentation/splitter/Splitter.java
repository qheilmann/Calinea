package io.calinea.segmentation.splitter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

import java.util.List;

import io.calinea.segmentation.SegmentationResult;
import io.calinea.segmentation.SegmentationState;
import io.calinea.segmentation.measurer.ComponentMeasurer;

public class Splitter {

    private final SplitterConfig config;
    private final ComponentMeasurer measurer;

    public Splitter(SplitterConfig config, ComponentMeasurer measurer) {
        this.config = config;
        this.measurer = measurer;
    }

    public SegmentationResult split(Component component) {
        SegmentationState state = new SegmentationState();
        traverse(component, state);
        return new SegmentationResult(state.finish());
    }

    private void traverse(Component component, SegmentationState state) {
        // 1. Push Style (Merge current component style with parent)
        state.pushStyle(component.style());

        // 2. Handle Content & Children
        if (measurer.isAtomic(component)) {
            handleAtomic(component, state);
        } else {
            handleSplittable(component, state);
        }

        // 3. Pop Style
        state.popStyle();
    }
    
    private void handleSplittable(Component component, SegmentationState state) {
        // Non-atomic component (TextComponent or TranslatableComponent)
        // We convert to a "text-like" representation for splitting purposes.
        TextComponent resolved = measurer.asTextComponent(component);
        
        if (resolved == null) {
             throw new IllegalStateException("Component " + component.getClass().getSimpleName() + " is not atomic but did not resolve to a TextComponent.");
        }
        
        String content = resolved.content();
        
        if (!content.isEmpty()) {
            handleText(content, state);
        }
        
        // If we replaced the component (e.g. Translatable -> Text structure), 
        // the 'resolved' component contains the full structure (content + args + original children).
        // So we must traverse the 'resolved' structure's children.
        // If it wasn't replaced (Standard TextComponent), 'resolved' is 'component', so we traverse its children.
        for (Component child : resolved.children()) {
            traverse(child, state);
        }
    }

    private void handleAtomic(Component component, SegmentationState state) {
        // Atomic component (e.g. Keybind, Score, Object, or unresolved Translatable if not converted to text)
        // We measure it as a whole block.
        
        // We use the *effective style* from the stack for measurement context if needed,
        // but the component itself retains its own style properties.
        
        // Create a shallow copy with the effective style for measurement
        Component atom = component.children(List.of()).style(state.currentStyle());
        
        // Note: measureRoot only measures the content of this component, not children
        double width = measurer.measureRoot(atom);
        
        if (state.currentWidth() + width <= config.maxWidth()) {
            state.append(atom, width);
        } else {
            // If it doesn't fit, wrap to new line
            if (state.currentWidth() > 0) {
                state.newLine();
            }
            // Append to new line (even if it overflows)
            state.append(atom, width);
        }

        // Atomic components can still have children that need to be traversed
        for (Component child : component.children()) {
            traverse(child, state);
        }
    }

    private void handleText(String text, SegmentationState state) {
        Style style = state.currentStyle();
        double maxWidth = config.maxWidth();
        
        List<String> tokens = config.tokenizer().tokenize(text);
        
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            
            if (token.equals("\n")) {
                state.newLine();
                continue;
            }
            
            // Measure the token using the helper in ComponentMeasurer
            double tokenWidth = measurer.measureText(token, style);
            
            if (state.currentWidth() + tokenWidth <= maxWidth) {
                state.append(Component.text(token, style), tokenWidth);
            } else {
                // Token doesn't fit.
                
                // If current line is not empty, wrap to new line first.
                if (state.currentWidth() > 0) {
                    state.newLine();
                }
                
                // If the token is whitespace and we are at the start of a line, skip it.
                // This prevents lines from starting with a space.
                if (state.currentWidth() == 0 && token.isBlank()) {
                    continue;
                }
                
                // Check if it fits on the new line
                if (tokenWidth <= maxWidth) {
                    state.append(Component.text(token, style), tokenWidth);
                } else {
                    // Token is too long even for a full line. Split by character.
                    splitByChar(token, style, state, maxWidth);
                }
            }
        }
    }

    private void splitByChar(String text, Style style, SegmentationState state, double maxWidth) {
        // Iterate by codepoints to handle unicode correctly
        int length = text.length();
        for (int offset = 0; offset < length; ) {
            int codepoint = text.codePointAt(offset);
            String charStr = new String(Character.toChars(codepoint));
            
            double charWidth = measurer.measureText(charStr, style);
            
            if (state.currentWidth() + charWidth > maxWidth) {
                state.newLine();
            }
            state.append(Component.text(charStr, style), charWidth);
            
            offset += Character.charCount(codepoint);
        }
    }
}
