package io.calinea;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.nio.file.Path;
import io.calinea.config.CalineaConfig;
import io.calinea.internal.ComponentMeasurer;
import io.calinea.logger.CalineaLogger;
import io.calinea.models.PackInfo;
import io.calinea.reader.JsonFontReader;

//TODO add nullmarked jspecify
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
 * int width = Calinea.measureWidth(component);
 * </pre>
 */
public class Calinea {

    public static final String NAMESPACE = "calinea";
    public static final String LIBRARY_NAME = "Calinea";
    
    private static PackInfo packInfo;
    private static CalineaConfig config;

    public static void onLoad(CalineaConfig config) {
        Calinea.config = config;
        
        // Load the json
        Path fontInfoPath = config.fontInfoPath();

        JsonFontReader reader = new JsonFontReader();
        try {
            packInfo = reader.readFonts(fontInfoPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load font info from " + fontInfoPath, e);
        }

        // Make manager
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

    public static PackInfo TMPgetPackInfo() {
        return packInfo;
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
    public static Component center(Component component, int totalWidth) {
        int componentWidth = measureWidth(component);
        int padding = Math.max(0, (totalWidth - componentWidth) / 2);
        return Component.text(" ".repeat(padding)).append(component);
    }
    
    /**
     * Aligns text to the left within a specific width.
     * 
     * @param text the text to align
     * @param totalWidth the total width to align within
     * @return left-aligned component
     */
    public static Component alignLeft(String text, int totalWidth) {
        return alignLeft(Component.text(text), totalWidth);
    }
    
    /**
     * Aligns a component to the left within a specific width.
     * 
     * @param component the component to align
     * @param totalWidth the total width to align within
     * @return left-aligned component
     */
    public static Component alignLeft(Component component, int totalWidth) {
        int componentWidth = measureWidth(component);
        int padding = Math.max(0, totalWidth - componentWidth);
        return component.append(Component.text(" ".repeat(padding)));
    }
    
    /**
     * Aligns text to the right within a specific width.
     * 
     * @param text the text to align
     * @param totalWidth the total width to align within
     * @return right-aligned component
     */
    public static Component alignRight(String text, int totalWidth) {
        return alignRight(Component.text(text), totalWidth);
    }
    
    /**
     * Aligns a component to the right within a specific width.
     * 
     * @param component the component to align
     * @param totalWidth the total width to align within
     * @return right-aligned component
     */
    public static Component alignRight(Component component, int totalWidth) {
        int componentWidth = measureWidth(component);
        int padding = Math.max(0, totalWidth - componentWidth);
        return Component.text(" ".repeat(padding)).append(component);
    }
    
    /**
     * Measures the pixel width of a component.
     * 
     * @param component the component to measure
     * @return width in pixels
     */
    public static int measureWidth(Component component) {
        return ComponentMeasurer.measureComponent(component);
    }
    
    /**
     * Measures the pixel width of text.
     * 
     * @param text the text to measure
     * @return width in pixels
     */
    public static int measureWidth(String text) {
        return measureWidth(Component.text(text));
    }
    
    /**
     * Creates a separator line of a specific width.
     * 
     * @param width the width in pixels
     * @return separator component
     */
    public static Component separator(int width) {
        int dashCount = width / 6; // Approximate width of a dash
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
