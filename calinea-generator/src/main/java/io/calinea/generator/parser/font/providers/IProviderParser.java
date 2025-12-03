package io.calinea.generator.parser.font.providers;

import com.fasterxml.jackson.databind.JsonNode;

import io.calinea.generator.parser.PackPathContext;
import io.calinea.pack.font.FontInfo;

import java.io.IOException;

/**
 * Interface for parsing specific font provider types from Minecraft font definitions.
 */
public interface IProviderParser {
    
    /**
     * Gets the provider type this parser handles.
     * 
     * @return the provider type string (e.g., "bitmap", "space", "reference")
     */
    String getType();
    
    /**
     * Parses the provider node and adds width/reference information to the font.
     * 
     * @param provider the JSON node representing the provider
     * @param fontInfo the font info to populate
     * @param context the parsing context (font directory, resource pack root, etc.)
     * @throws IOException if parsing fails
     */
    void parse(JsonNode provider, FontInfo fontInfo, PackPathContext context) throws IOException;
}
