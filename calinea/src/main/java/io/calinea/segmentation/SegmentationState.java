package io.calinea.segmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

public class SegmentationState {

    /** The stack of styles applied (merged with the top one) on the pointed component **/
    private final Stack<Style> styleStack = new Stack<>();
    /** The list of finalized splitted components **/
    private final List<ComponentLine> splittedComponents = new ArrayList<>();
    
    /** The components in the current line **/
    private final List<Component> currentLineComponents = new ArrayList<>();
    /** The current width of the pointed component **/
    private double currentLineWidth = 0;

    // Buffer for merging consecutive text components with the same style
    private final StringBuilder pendingText = new StringBuilder();
    private Style pendingStyle = null;

    public SegmentationState() {
        // Start with empty style
        styleStack.push(Style.empty());
    }

    public Style currentStyle() {
        return styleStack.isEmpty() ? Style.empty() : styleStack.peek();
    }

    public void pushStyle(Style style) {
        if (styleStack.isEmpty()) {
            styleStack.push(style);
        } else {
            styleStack.push(styleStack.peek().merge(style));
        }
    }

    public void popStyle() {
        if (!styleStack.isEmpty()) {
            styleStack.pop();
        }
    }

    public void append(Component component, double width) {
        // Try to merge if it is a simple TextComponent
        if (component instanceof TextComponent textComponent && textComponent.children().isEmpty()) {
            Style style = textComponent.style();
            if (pendingStyle != null && pendingStyle.equals(style)) {
                pendingText.append(textComponent.content());
                currentLineWidth += width;
                return;
            } else {
                flushPending();
                pendingStyle = style;
                pendingText.append(textComponent.content());
                currentLineWidth += width;
                return;
            }
        }

        flushPending();
        currentLineComponents.add(component);
        currentLineWidth += width;
    }

    private void flushPending() {
        if (pendingText.length() > 0) {
            // Use pendingStyle if set, otherwise empty style (though it should be set if text is present)
            Style style = pendingStyle != null ? pendingStyle : Style.empty();
            currentLineComponents.add(Component.text(pendingText.toString(), style));
            pendingText.setLength(0);
            pendingStyle = null;
        }
    }

    public void newLine() {
        flushPending();
        splittedComponents.add(new ComponentLine(buildLineComponent(), currentLineWidth));
        currentLineComponents.clear();
        currentLineWidth = 0;
    }

    public double currentWidth() {
        return currentLineWidth;
    }

    public List<ComponentLine> finish() {
        flushPending();
        if (currentLineWidth > 0 || splittedComponents.isEmpty()) {
            splittedComponents.add(new ComponentLine(buildLineComponent(), currentLineWidth));
        }
        return splittedComponents;
    }

    private Component buildLineComponent() {
        if (currentLineComponents.isEmpty()) {
            return Component.empty();
        }
        if (currentLineComponents.size() == 1) {
            return currentLineComponents.get(0);
        }
        // Use append for each component to add them as children
        TextComponent.Builder builder = Component.text();
        for (Component c : currentLineComponents) {
            builder.append(c);
        }
        return builder.build();
    }
}
