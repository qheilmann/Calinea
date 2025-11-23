package io.calinea.segmentation.splitter;

public class SplitterConfig {
    private final double maxWidth;
    private final TextTokenizer tokenizer;

    public SplitterConfig(double maxWidth) {
        this(maxWidth, new TextTokenizer.Default());
    }

    public SplitterConfig(double maxWidth, TextTokenizer tokenizer) {
        this.maxWidth = maxWidth;
        this.tokenizer = tokenizer;
    }

    public double maxWidth() {
        return maxWidth;
    }

    public TextTokenizer tokenizer() {
        return tokenizer;
    }
}
