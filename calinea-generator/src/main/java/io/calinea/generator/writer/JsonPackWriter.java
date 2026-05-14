package io.calinea.generator.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.calinea.generator.writer.sections.FontsSectionWriter;
import io.calinea.generator.writer.sections.ISectionWriter;
import io.calinea.generator.writer.sections.TranslationsSectionWriter;
import io.calinea.pack.PackInfo;
import io.calinea.pack.reader.JsonPackReader;

/**
 * Writes Calinea data in JSON format for easier debugging and manual editing.
 * Supports multiple sections (fonts, translations, etc.) through a modular design.
 * <p>
 * Usage example:
 * <pre>
 * JsonPackWriter writer = new JsonPackWriter(packInfo);
 * writer.write(outputPath);
 * </pre>
 */
public class JsonPackWriter {
    
    private static final String DESCRIPTION = "Calinea resource pack data file. "
        + "To convert between Unicode escape sequences (\\uXXXX) and visible characters, "
        + "use https://r12a.github.io/app-conversion/ (select 'JS/Java/C' and disable ES6). "
        + "Bitmap fonts already include 1px padding for inter-letter spacing.";
    
    private final ObjectMapper objectMapper;
    private final List<ISectionWriter> sections;
    
    public JsonPackWriter(PackInfo packInfo) {
        this.objectMapper = JsonMapper.builder()
            .configure(JsonWriteFeature.ESCAPE_NON_ASCII, true)
            .build();

        // Sections
        this.sections = new ArrayList<>();
        sections.add(new FontsSectionWriter(packInfo.fontsInfo()));
        sections.add(new TranslationsSectionWriter(packInfo.translationsInfo()));
    }
    
    /**
     * Writes all sections to the specified output file.
     * 
     * @param outputFile the path to write the JSON file to
     * @throws IOException if writing fails
     */
    public void write(Path outputFile) throws IOException {
        // Ensure parent directory exists
        Files.createDirectories(outputFile.getParent());
        
        // Build root JSON node
        ObjectNode root = buildRootNode();
        
        // Write with custom pretty formatting for better array indentation
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        objectMapper.writer(printer).writeValue(outputFile.toFile(), root);
        
        printSummary(outputFile);
    }
    
    private ObjectNode buildRootNode() {
        ObjectNode root = objectMapper.createObjectNode();
        
        // Metadata
        root.put("version", JsonPackReader.CURRENT_VERSION);
        root.put("format", JsonPackReader.FORMAT);
        root.put("description", DESCRIPTION);
        
        // Write all sections
        for (ISectionWriter section : sections) {
            if (section.hasData()) {
                section.writeSection(root);
            }
        }
        
        return root;
    }
    
    private void printSummary(Path outputFile) {
        System.out.println("Generated Calinea data file: " + outputFile);
        
        for (ISectionWriter section : sections) {
            if (section.hasData()) {
                section.printStatistics();
            } else {
                System.err.println("  - No " + section.getSectionName() + " data");
            }
        }
    }
}
