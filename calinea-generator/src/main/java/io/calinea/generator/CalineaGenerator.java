package io.calinea.generator;

import org.jspecify.annotations.Nullable;

import io.calinea.config.CalineaGeneratorDefault;
import io.calinea.generator.parser.MinecraftPackParser;
import io.calinea.generator.writer.JsonPackWriter;
import io.calinea.pack.PackInfo;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Calinea Generator - Calinea config generator
 * 
 * Tool for generating Calinea config files from Minecraft resource packs.
 */
public class CalineaGenerator {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        try {
            String resourcePackPath = args[0];
            String outputPath = args.length > 1 ? args[1] :  CalineaGeneratorDefault.DEFAULT_OUTPUT_DIR;
            
            printVersion();
            generateCalineaConfig(resourcePackPath, outputPath);
            
        } catch (Exception e) {
            handleError(e);
        }
    }
    
    /**
     * Prints the usage information for the application.
     */
    private static void printUsage() {
        System.out.println("\nUsage:");
        System.out.println("  java -jar calinea-generator.jar <resource-pack-path> [output-path]");
        System.out.println("\nDescription:");
        System.out.println("  Analyzes a Minecraft resource pack and generates a JSON file containing");
        System.out.println("  character width mappings for all fonts.");
        System.out.println("\nExamples:");
        System.out.println("  java -jar calinea-generator.jar ./my-resource-pack ./output");
        System.out.println("  # Creates: ./output/" + CalineaGeneratorDefault.DEFAULT_OUTPUT_FILENAME);
        System.out.println("  java -jar calinea-generator.jar ./my-resource-pack ./output/custom-name.json");
        System.out.println("  # Creates: ./output/custom-name.json");
    }
    
    /**
     * Prints the application version.
     */
    private static void printVersion() {
        String version = getVersion();
        System.out.println("Calinea Generator v" + version);
    }
    
    /**
     * Gets the application version from the package manifest.
     * 
     * @return the version string, or "dev" if not available
     */
    private static String getVersion() {
        @Nullable String version = CalineaGenerator.class.getPackage().getImplementationVersion();
        return (version != null && !version.isEmpty()) ? version : "dev";
    }
    
    /**
     * Resolves the output path, determining whether it's a directory or a full file path.
     * If it's a directory, appends the default filename. If it's a file, uses it as-is.
     * 
     * @param outputPath the output path provided by the user
     * @return the resolved full path to the output file
     */
    private static Path resolveOutputPath(String outputPath) {
        Path path = Paths.get(outputPath).normalize();
        
        // If the path ends with .json, treat it as a complete file path
        if (outputPath.toLowerCase().endsWith(".json")) {
            return path;
        }
        
        // Otherwise, treat it as a directory and append the default filename
        return path.resolve(CalineaGeneratorDefault.DEFAULT_OUTPUT_FILENAME);
    }
    
    /**
     * Generates the Calinea config JSON file from the resource pack.
     * 
     * @param resourcePackPath the path to the Minecraft resource pack
     * @param outputPath the output path (directory or full file path)
     * @throws Exception if an error occurs during generation
     */
    private static void generateCalineaConfig(String resourcePackPath, String outputPath) throws Exception {
        System.out.println("\nAnalyzing resource pack: " + resourcePackPath);
        
        Path resourcePack = Paths.get(resourcePackPath);
        Path outputFilePath = resolveOutputPath(outputPath);
        
        // Parse the resource pack
        PackInfo packInfo = parsePackInfo(resourcePack);

        // Write the output JSON file
        writePackInfo(packInfo, outputFilePath);
        
        System.out.println("\nGeneration complete! File saved to: " + outputFilePath.toAbsolutePath());
    }
    
    /**
     * Parses all fonts from the resource pack.
     * 
     * @param resourcePack the resource pack path
     * @return PackInfo containing parsed font information
     * @throws Exception if parsing fails
     */
    private static PackInfo parsePackInfo(Path resourcePack) throws Exception {
        MinecraftPackParser parser = new MinecraftPackParser();
        PackInfo packInfo = parser.parseResourcePack(resourcePack);
        parser.printStatistics(packInfo);
        return packInfo;
    }
    
    /**
     * Writes all calinea config to a JSON file.
     * 
     * @param packInfo the PackInfo to write
     * @param outputFilePath the full path to the output file
     * @throws Exception if writing fails
     */
    private static void writePackInfo(PackInfo packInfo, Path outputFilePath) throws Exception {
        System.out.println("\nGenerating calinea config file...");
        
        JsonPackWriter writer = new JsonPackWriter(packInfo);
        writer.write(outputFilePath);
    }
    
    /**
     * Handles and reports errors that occur during execution.
     * 
     * @param e the exception that occurred
     */
    private static void handleError(Exception e) {
        System.err.println("\n✗ Error: " + e.getMessage());
        e.printStackTrace();
        System.exit(1);
    }
}
