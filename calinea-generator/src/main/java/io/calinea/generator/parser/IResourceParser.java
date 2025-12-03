package io.calinea.generator.parser;

import java.nio.file.Path;

/**
 * Interface for parsers that process a specific type of content from a resource pack.
 * Parsers are stateless - they take input and return output in a single operation.
 */
public interface IResourceParser<T> {
    
    /**
     * Parses content from a resource pack.
     * 
     * @param resourcePackRoot the root of the resource pack
     * @return the parsed result
     */
    T parse(Path resourcePackRoot);
    
    /**
     * Validates the parsed content and reports any issues.
     * 
     * @param result the parsed result to validate
     */
    void validate(T result);
    
    /**
     * Prints statistics about the parsed content.
     * 
     * @param result the parsed result to report on
     */
    void printStatistics(T result);
}
