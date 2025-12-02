package io.calinea.pack.reader;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Interface for readers that parse a specific section of the Calinea config JSON.
 *
 * @param <T> the type of data this reader produces
 */
public interface ISectionReader<T> {
    
    /**
     * Gets the section name this reader handles (e.g., "fonts", "keybinds", "translations").
     *
     * @return the section name in the JSON
     */
    String getSectionName();
    
    /**
     * Reads and parses the section from a JSON node.
     *
     * @param sectionNode the JSON node for this section
     * @return the parsed result
     * @throws IOException if parsing fails
     */
    T read(JsonNode sectionNode) throws IOException;
}
