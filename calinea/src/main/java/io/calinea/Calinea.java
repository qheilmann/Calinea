package io.calinea;

import java.nio.file.Path;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.Nullable;

import io.calinea.config.CalineaConfig;
import io.calinea.font.PackInfo;
import io.calinea.font.reader.JsonFontReader;
import io.calinea.logger.CalineaLogger;
import io.calinea.resolver.ComponentResolver;
import io.calinea.segmentation.SegmentationResult;
import io.calinea.segmentation.measurer.ComponentMeasurer;
import io.calinea.segmentation.measurer.ComponentMeasurerConfig;
import io.calinea.segmentation.splitter.Splitter;
import io.calinea.segmentation.splitter.SplitterConfig;
import io.calinea.segmentation.splitter.TextTokenizer;
import io.calinea.space.SpaceFont;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Calinea - Adventure Component Manipulation Library
 * 
 * Main API class providing static utility methods for manipulating Adventure components.
 * This is the primary entry point for all Calinea functionality.
 * 
 * Basic usage:
 * <pre>
 * Component centered = Calinea.center("Hello World!");
 * Component aligned = Calinea.alignLeft("Left text", 100);
 * double width = Calinea.measureWidth(component);
 * </pre>
 */
public class Calinea {

    public static final String NAMESPACE = "calinea";
    public static final String LIBRARY_NAME = "Calinea";

    private static @Nullable PackInfo packInfo;
    private static @Nullable CalineaConfig config;

    public static void onLoad(CalineaConfig config) {
        Calinea.config = config;
        reloadFonts();
    }

    public static CalineaConfig getConfig() {
        if (config == null) {
            throw new IllegalStateException("Tried to access Calinea config, but it was not initialized! Are you using Calinea features before calling Calinea#onLoad?");
        }
        return config;
    }

    public static CalineaLogger getLogger() {
        return getConfig().logger();
    }

    public static void reloadFonts() {

        Path fontInfoPath = getConfig().fontInfoPath();
        JsonFontReader reader = new JsonFontReader();

        try {
            packInfo = reader.readFonts(fontInfoPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load font info from " + fontInfoPath, e);
        }
    }

    public static Component resolve(Component component, CommandSender context, Entity scoreboardSubject) {
        ComponentResolver resolver = new ComponentResolver();
        return resolver.resolve(component, context, scoreboardSubject);
    }
    
    public static double measure(Component component) {
        ComponentMeasurer measurer = new ComponentMeasurer(new ComponentMeasurerConfig(packInfo));
        return measurer.measure(component);
    }
    
    public static double resolveAndMeasure(Component component, CommandSender context, Entity scoreboardSubject) {
        component = resolve(component, context, scoreboardSubject);
        return measureWidth(component);
    }

    public static SegmentationResult split(Component component, int maxWidth) {
        ComponentMeasurer measurer = new ComponentMeasurer(new ComponentMeasurerConfig(packInfo));
        SplitterConfig splitterConfig = new SplitterConfig(maxWidth, new TextTokenizer.Default());
        Splitter splitter = new Splitter(splitterConfig, measurer);
        return splitter.split(component);
    }


    /**
     * Centers text within the default Minecraft chat width (320 pixels).
     * 
     * @param text the text to center
     * @return centered component
     */
    public static Component center(String text) {
        return center(Component.text(text));
    }
    
    /**
     * Centers a component within the default Minecraft chat width (320 pixels).
     * 
     * @param component the component to center
     * @return centered component
     */
    public static Component center(Component component) {
        return center(component, 320); // Default Minecraft chat width
    }
    
    /**
     * Centers a component within a specific width.
     * 
     * @param component the component to center
     * @param totalWidth the total width to center within
     * @return centered component
     */
    public static Component center(Component component, double totalWidth) {
        double componentWidth = measureWidth(component);
        double sideWidth = (totalWidth - componentWidth) / 2;
        Calinea.getLogger().info(String.valueOf(sideWidth));
        Component sideComponent = Component.text(SpaceFont.space(sideWidth));
        return Component.textOfChildren(sideComponent, component, sideComponent);
    }

    /**
     * Aligns text to the left within a specific width.
     * 
     * @param text the text to align
     * @param totalWidth the total width to align within
     * @return left-aligned component
     */
    public static Component alignLeft(String text, double totalWidth) {
        return alignLeft(Component.text(text), totalWidth);
    }
    
    /**
     * Aligns a component to the left within a specific width.
     * 
     * @param component the component to align
     * @param totalWidth the total width to align within
     * @return left-aligned component
     */
    public static Component alignLeft(Component component, double totalWidth) {
        double componentWidth = measureWidth(component);
        double padding = Math.max(0, totalWidth - componentWidth) / 4; // Divide by 4 for space character width
        return component.append(Component.text(" ".repeat((int) padding)));
    }
    
    /**
     * Aligns text to the right within a specific width.
     * 
     * @param text the text to align
     * @param totalWidth the total width to align within
     * @return right-aligned component
     */
    public static Component alignRight(String text, double totalWidth) {
        return alignRight(Component.text(text), totalWidth);
    }
    
    /**
     * Aligns a component to the right within a specific width.
     * 
     * @param component the component to align
     * @param totalWidth the total width to align within
     * @return right-aligned component
     */
    public static Component alignRight(Component component, double totalWidth) {
        double componentWidth = measureWidth(component);
        double padding = Math.max(0, totalWidth - componentWidth) / 4; // Divide by 4 for space character width
        return Component.text(" ".repeat((int) padding)).append(component);
    }
    
    /**
     * Measures the pixel width of a component.
     * 
     * @param component the component to measure
     * @return width in pixels
     */
    public static double measureWidth(Component component) {
        ComponentMeasurer measurer = new ComponentMeasurer(new ComponentMeasurerConfig(packInfo));
        return measurer.measure(component);
    }
    
    /**
     * Measures the pixel width of text.
     * 
     * @param text the text to measure
     * @return width in pixels
     */
    public static double measureWidth(String text) {
        return measureWidth(Component.text(text));
    }
    
    /**
     * Creates a separator line of a specific width.
     * 
     * @param width the width in pixels
     * @return separator component
     */
    public static Component separator(double width) {
        int dashCount = (int) (width / 5); // Approximate width of a dash
        return Component.text("-".repeat(dashCount)).color(NamedTextColor.GRAY);
    }
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private Calinea() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }
}
