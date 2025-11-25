package io.calinea.segmentation.splitter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

import java.util.List;

import io.calinea.segmentation.SegmentationResult;
import io.calinea.segmentation.SegmentationState;
import io.calinea.segmentation.measurer.ComponentMeasurer;

public class Splitter {

    private final TextTokenizer tokenizer;
    private final ComponentMeasurer measurer;

    public Splitter(TextTokenizer tokenizer, ComponentMeasurer measurer) {
        this.tokenizer = tokenizer;
        this.measurer = measurer;
    }

    public SegmentationResult split(Component component, double maxWidth) {
        SegmentationState state = new SegmentationState();
        traverse(component, state, maxWidth);
        return new SegmentationResult(state.finish());
    }

    private void traverse(Component component, SegmentationState state, double maxWidth) {
        // 1. Push Style (Merge current component style with parent)
        state.pushStyle(component.style());

        // 2. Handle Content & Children
        if (measurer.isAtomic(component)) {
            handleAtomic(component, state, maxWidth);
        } else {
            handleSplittable(component, state, maxWidth);
        }

        // 3. Pop Style
        state.popStyle();
    }
    
    private void handleSplittable(Component component, SegmentationState state, double maxWidth) {
        // Non-atomic component (TextComponent or TranslatableComponent)
        // We convert to a "text-like" representation for splitting purposes.
        TextComponent resolved = measurer.asTextComponent(component);
        
        if (resolved == null) {
             throw new IllegalStateException("Component " + component.getClass().getSimpleName() + " is not atomic but did not resolve to a TextComponent.");
        }
        
        String content = resolved.content();
        
        if (!content.isEmpty()) {
            handleText(content, state, maxWidth);
        }
        
        // If we replaced the component (e.g. Translatable -> Text structure), 
        // the 'resolved' component contains the full structure (content + args + original children).
        // So we must traverse the 'resolved' structure's children.
        // If it wasn't replaced (Standard TextComponent), 'resolved' is 'component', so we traverse its children.
        for (Component child : resolved.children()) {
            traverse(child, state, maxWidth);
        }
    }

    private void handleAtomic(Component component, SegmentationState state, double maxWidth) {
        // Atomic component (e.g. Keybind, Score, Object, or unresolved Translatable if not converted to text)
        // We measure it as a whole block.
        
        // We use the *effective style* from the stack for measurement context if needed,
        // but the component itself retains its own style properties.
        
        // Create a shallow copy with the effective style for measurement
        Component atom = component.children(List.of()).style(state.currentStyle());
        
        // Note: measureRoot only measures the content of this component, not children
        double width = measurer.measureRoot(atom);
        
        if (state.currentWidth() + width <= maxWidth) {
            state.append(atom, width);
        } else {
            // If current line is not empty, wrap to new line first.
            if (state.currentWidth() > 0) {
                state.trimTrailingSpace(measurer);
                state.newLine();
            }
            // Append to new line (even if it overflows)
            state.append(atom, width);
        }

        // Atomic components can still have children that need to be traversed
        for (Component child : component.children()) {
            traverse(child, state, maxWidth);
        }
    }

    private void handleText(String text, SegmentationState state, double maxWidth) {
        Style style = state.currentStyle();
        
        List<String> tokens = tokenizer.tokenize(text);
        
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            
            if (token.equals("\n")) {
                state.trimTrailingSpace(measurer);
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
                    state.trimTrailingSpace(measurer);
                    state.newLine();
                }
                
                // If the token is whitespace and we are at the start of a line, skip it.
                // This prevents lines from starting with a space.
                if (token.isBlank() && state.currentWidth() == 0) {
                    continue;
                }
                
                // If the token itself is wider than maxWidth, we must split it by character.
                if (tokenWidth > maxWidth) {
                    splitByChar(token, style, state, maxWidth);
                } else {
                    state.append(Component.text(token, style), tokenWidth);
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
