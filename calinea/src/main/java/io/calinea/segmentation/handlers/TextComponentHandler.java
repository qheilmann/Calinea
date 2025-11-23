package io.calinea.segmentation.handlers;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.calinea.font.PackInfo;
import io.calinea.segmentation.measurer.ComponentMeasurerConfig;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

public class TextComponentHandler implements IComponentLayoutHandler<TextComponent> {
    private final ComponentMeasurerConfig config;

    public TextComponentHandler(ComponentMeasurerConfig config) {
        this.config = config;
    }

    @Override
    public boolean canHandle(Component component) {
        return component instanceof TextComponent;
    }

    @Override
    public double measureRoot(TextComponent component) {
        if (component == Component.empty() || component.content().isEmpty()) return 0;
        if (component == Component.newline() || component.content() == "\n") return 0;

        String text = component.content();
        Key fontKey = component.font();
        boolean isBold = component.style().hasDecoration(TextDecoration.BOLD);

        return measureTextWidth(text, fontKey, isBold);
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public TextComponent asTextComponent(TextComponent component) {
        return component;
    }


    /**
     * Measures the pixel width of plain text.
     * @param text the text to measure
     * @param fontKey the font key or {@link Style#DEFAULT_FONT} if null
     * @param isBold whether the text is bold
     * @return width in pixels
     */
    public double measureTextWidth(String text, @Nullable Key fontKey, boolean isBold) {
        if (text.isEmpty()) {
            return 0;
        }

        if (fontKey == null) {
            fontKey = Style.DEFAULT_FONT;
        }
        
        double totalWidth = 0;

        // A string may contain Unicode codepoints beyond BMP, so character can use multiple indexes, so we need to iterate by codepoints
        int codepointLength = text.codePointCount(0, text.length());
        for (int codepointOffset = 0; codepointOffset < codepointLength; codepointOffset++) {
            int index = text.offsetByCodePoints(0, codepointOffset);
            int codepoint = text.codePointAt(index);

            if (codepoint == '\n') {
                continue; // Newline has no width
            }

            totalWidth += measureCodepointWidth(codepoint, fontKey, isBold);
        }

        return totalWidth;
    }

    /**
     * Gets the width of a character in a specific font, accounting for bold style.
     * 
     * @param codepoint the character codepoint
     * @param fontKey the font key
     * @param isBold whether the character is bold
     * @return width in pixels
     */
    private double measureCodepointWidth(int codepoint, Key fontKey, boolean isBold) {
        PackInfo packInfo = config.getPackInfo();
        double width = packInfo.getWidth(fontKey, codepoint);
        if (isBold) {
            width += 1; // Bold duplicates the glyph 1px to the right, increasing width by 1px
        }
        width += 1; // 1px letter spacing
        return width;
    }
}
