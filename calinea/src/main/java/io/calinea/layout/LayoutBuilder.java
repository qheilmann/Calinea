package io.calinea.layout;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.Nullable;

import io.calinea.Calinea;
import io.calinea.segmentation.ComponentLine;
import io.calinea.segmentation.SegmentationResult;
import io.calinea.segmentation.splitter.Splitter;
import io.calinea.space.SpaceFont;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

/**
 * A fluent builder for creating complex component layouts.
 * <p>
 * This builder allows you to configure width, alignment, padding, and resolution context
 * before generating the final component. It handles text segmentation (wrapping) and
 * precise pixel-based alignment using {@link SpaceFont}.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * Component layout = new LayoutBuilder(content)
 *     .width(200)
 *     .align(Alignment.CENTER)
 *     .padding(10)
 *     .build();
 * </pre>
 * </p>
 */
public class LayoutBuilder {
    private final Component root;
    private double width = 150; // Default Minecraft dialog width
    private Alignment alignment = Alignment.LEFT;
    private double paddingLeft;
    private double paddingRight;
    private boolean fillLines = false;
    private CommandSender sender;
    private Entity subject;
    private LayoutContext context;

    /**
     * Creates a new LayoutBuilder for the given component.
     *
     * @param content the component to layout
     */
    public LayoutBuilder(Component content) {
        this.root = content;
    }

    /**
     * Sets the target width of the display area for this layout.
     * <p>
     * Content will be wrapped to fit within this width minus the applied padding.
     * Default is 150 pixels (standard Minecraft dialog width).
     *
     * Note: The width should not exceed the maximum width of the display area (e.g. 320 px for chat or 1024 px for dialogs).
     * The contentWidth is calculated as (width - paddingLeft - paddingRight).
     * </p>
     *
     * @param targetWidth the width in pixels
     * @return this builder
     */
    public LayoutBuilder width(double targetWidth) {
        this.width = targetWidth;
        return this;
    }

    /**
     * Sets the alignment strategy for the content.
     * <p>
     * Determines how content is positioned within the available width.
     * Default is {@link Alignment#LEFT}.
     * </p>
     *
     * @param alignment the alignment to use
     * @return this builder
     */
    public LayoutBuilder align(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    /**
     * Sets the left and right padding.
     * <p>
     * Padding reduces the available width for content.
     * </p>
     *
     * @param left  the left padding in pixels
     * @param right the right padding in pixels
     * @return this builder
     */
    public LayoutBuilder padding(double left, double right) {
        this.paddingLeft = left;
        this.paddingRight = right;
        return this;
    }

    /**
     * Sets the same padding for both left and right sides.
     *
     * @param padding the padding in pixels
     * @return this builder
     */
    public LayoutBuilder padding(double padding) {
        return padding(padding, padding);
    }

    /**
     * Sets whether to fill the remaining space on the line with empty space.
     * <p>
     * If true, space characters will be appended to the right of the content
     * to ensure the line takes up the full target width. This is useful for
     * preventing auto-centering behaviors in some Minecraft UI elements.
     * </p>
     *
     * @param fillLines true to fill lines, false otherwise
     * @return this builder
     */
    public LayoutBuilder fillLines(boolean fillLines) {
        this.fillLines = fillLines;
        return this;
    }

    /**
     * Sets the context for resolving components (e.g., selectors, scores).
     *
     * @param sender  the command sender to resolve relative to
     * @param subject the entity subject for scores (can be null if no ScoreComponents are used)
     * @return this builder
     */
    public LayoutBuilder resolve(CommandSender sender, @Nullable Entity subject) {
        this.sender = sender;
        this.subject = subject;
        return this;
    }

    /**
     * Sets the context for resolving components (e.g., selectors, scores)
     * <p>
     * This is a convenience method that uses the HumanEntity as both
     * the sender and the subject.
     * </p>
     *
     * @param entity the entity to resolve relative to
     * @return this builder
     */
    public LayoutBuilder resolve(Entity entity) {
        return resolve(entity, entity);
    }

    /**
     * Overrides the default {@link LayoutContext}.
     * <p>
     * Use this to provide custom tokenizers, measurers, or font info.
     * </p>
     *
     * @param context the layout context to use
     * @return this builder
     */
    public LayoutBuilder layoutContext(LayoutContext context) {
        this.context = context;
        return this;
    }

    /**
     * Builds the final component with the configured layout.
     * <p>
     * This process involves:
     * <ol>
     *     <li>Resolving the component (if a sender is provided).</li>
     *     <li>Splitting the component into lines that fit the content width.</li>
     *     <li>Applying alignment and padding to each line.</li>
     *     <li>Joining lines with newlines.</li>
     * </ol>
     * </p>
     *
     * @return the laid-out component
     */
    public Component build() {

        // Use the provided context or default to Calinea's layout context
        LayoutContext ctx = this.context;
        if (ctx == null) {
            ctx = Calinea.layoutContext();
        }

        // Resolve the component if a sender is provided
        Component componentToLayout = root;
        if (sender != null) {
            componentToLayout = ctx.componentResolver().resolve(root, sender, subject);
        }

        // Calculate content width after padding
        double contentWidth = width - paddingLeft - paddingRight;
        if (contentWidth <= 0) {
            throw new IllegalArgumentException("Content width must be positive after applying padding.");
        }

        // Split the component into lines within the content width
        Splitter splitter = ctx.splitter();
        SegmentationResult result = splitter.split(componentToLayout, contentWidth);

        TextComponent.Builder finalComponent = Component.text();
        boolean first = true;

        // Process each line to apply alignment and padding
        for (ComponentLine line : result.lines()) {
            if (!first) {
                finalComponent.append(Component.newline());
            }
            first = false;

            // Calculate alignment spacing
            double alignmentSpacing = 0;
            switch (alignment) {
                case LEFT:
                    alignmentSpacing = 0;
                    break;
                case CENTER:
                    alignmentSpacing = (contentWidth - line.width()) / 2;
                    break;
                case RIGHT:
                    alignmentSpacing = contentWidth - line.width();
                    break;
            }

            // Add left padding + alignment space
            double totalLeftPadding = paddingLeft + alignmentSpacing;
            if (totalLeftPadding != 0) {
                finalComponent.append(Component.text(SpaceFont.space(totalLeftPadding)));
            }
            
            // Add the line content
            finalComponent.append(line.component());

            // Add right padding / fill space
            if (fillLines) {
                double rightSpacing = contentWidth - alignmentSpacing - line.width();
                double totalRightPadding = rightSpacing + paddingRight;
                if (totalRightPadding > 0) {
                    finalComponent.append(Component.text(SpaceFont.space(totalRightPadding)));
                }
            }
        }

        return finalComponent.build();
    }
}
