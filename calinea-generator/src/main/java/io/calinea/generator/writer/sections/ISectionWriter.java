package io.calinea.generator.writer.sections;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Interface for writing a specific section of the JSON output file.
 */
public interface ISectionWriter {
    
    /**
     * Gets the name of this section for logging purposes.
     * 
     * @return the section name (e.g., "fonts", "keybinds", "translations")
     */
    String getSectionName();
    
    /**
     * Writes this section's data to the root JSON node.
     * 
     * @param root the root ObjectNode to write to
     */
    void writeSection(ObjectNode root);
    
    /**
     * Prints statistics about the written section to console.
     */
    void printStatistics();
    
    /**
     * Checks if this section has any data to write.
     * 
     * @return true if the section has data, false otherwise
     */
    boolean hasData();
}
