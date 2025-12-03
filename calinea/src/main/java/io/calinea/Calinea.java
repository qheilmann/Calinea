package io.calinea;

import java.nio.file.Path;
import java.util.Collections;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.Nullable;

import io.calinea.config.CalineaConfig;
import io.calinea.layout.Alignment;
import io.calinea.layout.LayoutBuilder;
import io.calinea.layout.LayoutContext;
import io.calinea.logger.CalineaLogger;
import io.calinea.pack.PackInfo;
import io.calinea.pack.reader.JsonPackReader;
import io.calinea.segmentation.SegmentationResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;

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
    private static @Nullable LayoutContext defaultLayoutContext;

    private Calinea() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }

    /**
     * Initializes Calinea with the given configuration.
     * <p>
     * This should be called once during application/plugin startup.
     * </p>
     * 
     * @param config the Calinea configuration
     */
    public static void onLoad(CalineaConfig config) {
        Calinea.config = config;
        reloadPackInfo();
    }

    /**
     * Gets the current Calinea configuration.
     * 
     * @return the Calinea configuration
     */
    public static CalineaConfig config() {
        if (config == null) {
            throw new IllegalStateException("Tried to access Calinea config, but it was not initialized! Are you using Calinea features before calling Calinea#onLoad?");
        }
        return config;
    }

    /**
     * Gets the Calinea logger.
     * 
     * @return the Calinea logger
     */
    public static CalineaLogger logger() {
        return config().logger();
    }

    /**
     * Gets the current LayoutContext.
     * 
     * @return the layout context
     */
    public static LayoutContext defaultLayoutContext() {
        if (defaultLayoutContext == null) {
            throw new IllegalStateException("Tried to access Calinea layout context, but it was not initialized! Are you using Calinea features before calling Calinea#onLoad?");
        }
        return defaultLayoutContext;
    }

    public static void reloadPackInfo() {

        Path calineaConfigPath = config().calineaConfigPath();

        try {
            packInfo = createPackInfo(calineaConfigPath);
            defaultLayoutContext = new LayoutContext.Builder(packInfo).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load calinea config from " + calineaConfigPath.toAbsolutePath(), e);
        }
    }

    //#region Static API Methods

    /**
     * Creates a new LayoutBuilder for the given component.
     * <p>
     * This is the entry point for creating complex layouts with fluent API.
     * </p>
     * 
     * @param component the component to layout
     * @return a new LayoutBuilder
     */
    public static LayoutBuilder layout(Component component) {
        return new LayoutBuilder(component);
    }
    
    /**
     * Centers a component within a specific width.
     * 
     * @param component the component to center
     * @param totalWidth the total width to center within
     * @return centered component
     */
    public static Component center(Component component, double totalWidth) {
        return new LayoutBuilder(component)
            .width(totalWidth)
            .align(Alignment.CENTER)
            .build();
    }
   
    /**
     * Aligns a component to the left within a specific width.
     * 
     * @param component the component to align
     * @param totalWidth the total width to align within
     * @return left-aligned component
     */
    public static Component alignLeft(Component component, double totalWidth) {
        return alignLeft(component, totalWidth, 0);
    }

    /**
     * Aligns a component to the left within a specific width.
     * 
     * @param component the component to align
     * @param totalWidth the total width to align within
     * @param leftPadding the left padding in pixels
     * @return left-aligned component
     */
    public static Component alignLeft(Component component, double totalWidth, double leftPadding) {
        return new LayoutBuilder(component)
            .width(totalWidth)
            .align(Alignment.LEFT)
            .padding(leftPadding, 0)
            .build();
    }
    
    /**
     * Aligns a component to the right within a specific width.
     * 
     * @param component the component to align
     * @param totalWidth the total width to align within
     * @return right-aligned component
     */
    public static Component alignRight(Component component, double totalWidth) {
        return alignRight(component, totalWidth, 0);
    }

    /**
     * Aligns a component to the right within a specific width.
     * 
     * @param component the component to align
     * @param totalWidth the total width to align within
     * @param rightPadding the right padding in pixels
     * @return right-aligned component
     */
    public static Component alignRight(Component component, double totalWidth, double rightPadding) {
        return new LayoutBuilder(component)
            .width(totalWidth)
            .align(Alignment.RIGHT)
            .padding(0, rightPadding)
            .build();
    }

    /**
     * Resolves a component in the given context.
     * 
     * @param component the component to resolve
     * @param entity the entity for both context and scoreboard resolution
     * @return resolved component
     */
    public static Component resolve(Component component, Entity entity) {
        return resolve(component, entity, entity);
    }
    
    /**
     * Resolves a component in the given context.
     * 
     * @param component the component to resolve
     * @param context the command sender context
     * @param scoreboardSubject the entity for scoreboard resolution
     * @return resolved component
     */
    public static Component resolve(Component component, CommandSender context, Entity scoreboardSubject) {
        return defaultLayoutContext().componentResolver().resolve(component, context, scoreboardSubject);
    }

    /**
     * Measures the pixel width of a component.
     * 
     * @param component the component to measure
     * @return width in pixels
     */
    public static double measure(Component component) {
        return defaultLayoutContext().componentMeasurer().measure(component);
    }
    
    /**
     * Resolves and then measures the pixel width of a component.
     * 
     * @param component the component to resolve and measure
     * @param context the command sender context
     * @param scoreboardSubject the entity for scoreboard resolution
     * @return width in pixels
     */
    public static double resolveAndMeasure(Component component, CommandSender context, Entity scoreboardSubject) {
        component = resolve(component, context, scoreboardSubject);
        return measure(component);
    }

    /**
     * Splits a component into segments that fit within the specified width.
     * 
     * @param component the component to split
     * @param maxWidth the maximum width in pixels
     * @return segmentation result containing the segments
     */
    public static SegmentationResult split(Component component, int maxWidth) {
        return defaultLayoutContext().splitter().split(component, maxWidth);
    }
    
    /**
     * Creates a centered separator line of a specific width.
     * 
     * @param component the component to use as the separator
     * @param width the width in pixels
     * @param repeatToFill whether to repeat the component to fill the width
     * @return separator component
     */
    public static Component separator(Component component, double width, boolean repeatToFill) {
        double componentWidth = measure(component);

        if (repeatToFill) {
            int repeatCount = (int) Math.floor(width / componentWidth);
            component = Component.join(JoinConfiguration.noSeparators(),
                Collections.nCopies(repeatCount, component)
            );
            componentWidth = componentWidth * repeatCount;
        }

        return Calinea.layout(component)
            .width(width)
            .align(Alignment.CENTER)
            .fillLines(repeatToFill) // If repeating, fill the line completely
            .build();
    }

    /**
     * Creates a new LayoutContext.Builder with the given pack info.
     * <p>
     * Use this to create a custom layout context with specific configuration.
     * </p>
     * 
     * @param packInfo the pack info to use
     * @return a new LayoutContext.Builder
     */
    public static LayoutContext.Builder createContext(PackInfo packInfo) {
        return new LayoutContext.Builder(packInfo);
    }

    /**
     * Creates PackInfo from the given font info path.
     * 
     * @param fontInfoPath the path to the font info JSON file
     * @return the created PackInfo
     */
    public static PackInfo createPackInfo(Path packInfoPath) {
        JsonPackReader packReader = new JsonPackReader();

        try {
            return packReader.read(packInfoPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load pack info from " + packInfoPath.toAbsolutePath(), e);
        }
    }

    //#endregion Static API Methods
}
