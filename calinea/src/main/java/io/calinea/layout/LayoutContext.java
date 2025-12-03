package io.calinea.layout;

import io.calinea.pack.PackInfo;
import io.calinea.resolver.ComponentResolver;
import io.calinea.resolver.IComponentResolver;
import io.calinea.segmentation.measurer.ComponentMeasurer;
import io.calinea.segmentation.measurer.ComponentMeasurerConfig;
import io.calinea.segmentation.measurer.IComponentMeasurer;
import io.calinea.segmentation.splitter.TextTokenizer;
import io.calinea.segmentation.splitter.Splitter;

/**
 * Holds the context and services required for performing layout operations.
 * <p>
 * This includes information about the resource pack (fonts), measuring
 * tokenizing text, and the splitter logic.
 * </p>
 * <p>
 * Use {@link Builder} to create custom instances, or access the default
 * instance via {@link io.calinea.Calinea#defaultLayoutContext()}.
 * </p>
 */
public class LayoutContext {
    private final PackInfo packInfo;
    private final TextTokenizer textTokenizer;
    private final IComponentResolver componentResolver;
    private final IComponentMeasurer componentMeasurer;
    private final Splitter splitter;

    /**
     * Creates a new LayoutContext.
     *
     * @param packInfo          the resource pack information
     * @param textTokenizer     the tokenizer for splitting text
     * @param componentResolver the resolver for translating components
     */
    public LayoutContext(PackInfo packInfo, TextTokenizer textTokenizer, IComponentResolver componentResolver, IComponentMeasurer componentMeasurer) {
        this.packInfo = packInfo;
        this.textTokenizer = textTokenizer;
        this.componentResolver = componentResolver;
        this.componentMeasurer = componentMeasurer;
        this.splitter = new Splitter(textTokenizer, componentMeasurer);
    }

    /**
     * Gets the resource pack information.
     *
     * @return the pack info
     */
    public PackInfo packInfo() {
        return packInfo;
    }

    /**
     * Gets the text tokenizer.
     *
     * @return the tokenizer
     */
    public TextTokenizer textTokenizer() {
        return textTokenizer;
    }

    /**
     * Gets the component resolver.
     *
     * @return the resolver
     */
    public IComponentResolver componentResolver() {
        return componentResolver;
    }

    /**
     * Gets the component measurer.
     *
     * @return the measurer
     */
    public IComponentMeasurer componentMeasurer() {
        return componentMeasurer;
    }

    /**
     * Gets the splitter service.
     *
     * @return the splitter
     */
    public Splitter splitter() {
        return splitter;
    }

    /**
     * Builder for creating {@link LayoutContext} instances.
     */
    public static class Builder {
        private final PackInfo packInfo;
        private TextTokenizer textTokenizer;
        private IComponentResolver componentResolver;
        private IComponentMeasurer componentMeasurer;

        /**
         * Creates a new builder with the required pack info.
         *
         * @param packInfo the resource pack information
         */
        public Builder(PackInfo packInfo) {
            this.packInfo = packInfo;
            this.textTokenizer = new TextTokenizer.Default();
            this.componentResolver = new ComponentResolver(packInfo);
            this.componentMeasurer = new ComponentMeasurer(new ComponentMeasurerConfig(packInfo));
        }

        /**
         * Sets a custom text tokenizer.
         *
         * @param textTokenizer the tokenizer to use
         * @return this builder
         */
        public Builder textTokenizer(TextTokenizer textTokenizer) {
            this.textTokenizer = textTokenizer;
            return this;
        }

        /**
         * Sets a custom component resolver.
         *
         * @param componentResolver the resolver to use
         * @return this builder
         */
        public Builder componentResolver(IComponentResolver componentResolver) {
            this.componentResolver = componentResolver;
            return this;
        }

        /**
         * Sets a custom component measurer.
         *
         * @param componentMeasurer the measurer to use
         * @return this builder
         */
        public Builder componentMeasurer(IComponentMeasurer componentMeasurer) {
            this.componentMeasurer = componentMeasurer;
            return this;
        }

        /**
         * Builds the LayoutContext.
         *
         * @return the new context
         */
        public LayoutContext build() {
            return new LayoutContext(packInfo, textTokenizer, componentResolver, componentMeasurer);
        }
    }
}
