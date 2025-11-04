package io.calinea.generator;

import org.jspecify.annotations.Nullable;

import io.calinea.generator.parser.MinecraftFontParser;
import io.calinea.generator.writer.JsonFontWriter;
import io.calinea.models.PackInfo;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Calinea Generator - Font Width Atlas Generator
 * 
 * Tool for generating width atlases for fonts inside a Minecraft resource pack.
 */
public class CalineaGenerator {
    
    private static final String DEFAULT_OUTPUT_DIR = "./calinea-output";
    private static final String OUTPUT_FILENAME = "font-widths.json";
    
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        try {
            String resourcePackPath = args[0];
            String outputPath = args.length > 1 ? args[1] : DEFAULT_OUTPUT_DIR;
            
            printVersion();
            generateFontWidths(resourcePackPath, outputPath);
            
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
        System.out.println("  # Creates: ./output/" + OUTPUT_FILENAME);
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
        return path.resolve(OUTPUT_FILENAME);
    }
    
    /**
     * Generates the font width mappings for the given resource pack.
     * 
     * @param resourcePackPath the path to the Minecraft resource pack
     * @param outputPath the output path (directory or full file path)
     * @throws Exception if an error occurs during generation
     */
    private static void generateFontWidths(String resourcePackPath, String outputPath) throws Exception {
        System.out.println("\nAnalyzing resource pack: " + resourcePackPath);
        
        Path resourcePack = Paths.get(resourcePackPath);
        Path outputFilePath = resolveOutputPath(outputPath);
        
        PackInfo packInfo = parseFonts(resourcePack);
        validateFonts(packInfo);
        writeFontWidths(packInfo, outputFilePath);
        
        printSuccess(outputFilePath, packInfo.getFonts().size());
    }
    
    /**
     * Parses all fonts from the resource pack.
     * 
     * @param resourcePack the resource pack path
     * @return PackInfo containing parsed font information
     * @throws Exception if parsing fails
     */
    private static PackInfo parseFonts(Path resourcePack) throws Exception {
        MinecraftFontParser parser = new MinecraftFontParser();
        return parser.parseResourcePack(resourcePack);
    }
    
    /**
     * Validates that fonts were found and prints font information.
     * 
     * @param packInfo the PackInfo to validate
     */
    private static void validateFonts(PackInfo packInfo) {
        if (packInfo.getFonts().isEmpty()) {
            System.out.println("No fonts found in resource pack!");
            return;
        }
        
        System.out.println("Found " + packInfo.getFonts().size() + " fonts:");
        packInfo.getFonts().values().forEach(font -> System.out.println("  - " + font));
    }
    
    /**
     * Writes the font width mappings to a JSON file.
     * 
     * @param packInfo the PackInfo to write
     * @param outputFilePath the full path to the output file
     * @throws Exception if writing fails
     */
    private static void writeFontWidths(PackInfo packInfo, Path outputFilePath) throws Exception {
        System.out.println("\nGenerating font width file...");
        
        JsonFontWriter writer = new JsonFontWriter();
        writer.writePackInfo(packInfo, outputFilePath);
    }
    
    /**
     * Prints success message with generation details.
     * 
     * @param outputFilePath the full path to the output file
     * @param fontCount the number of fonts processed
     */
    private static void printSuccess(Path outputFilePath, int fontCount) {
        System.out.println("\nGeneration complete! File saved to: " + outputFilePath.toAbsolutePath());
        System.out.println("This JSON file contains width mappings for all " + fontCount + " fonts");
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
