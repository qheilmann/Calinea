package io.calinea.generator.parser.font.providers;

import com.fasterxml.jackson.databind.JsonNode;

import io.calinea.generator.parser.font.FontParserContext;
import io.calinea.pack.font.FontInfo;

import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Parses space font providers that define fixed-width spacing characters.
 */
public class SpaceProviderParser implements IProviderParser {
    
    @Override
    public String getType() {
        return "space";
    }
    
    @Override
    public void parse(JsonNode provider, FontInfo fontInfo, FontParserContext context) throws IOException {
        @Nullable JsonNode advances = provider.get("advances");
        
        if (advances == null || !advances.isObject()) {
            System.err.println("Space provider missing or invalid 'advances' object");
            return;
        }
        
        // Iterate through all advances entries
        advances.fieldNames().forEachRemaining(charString -> {
            int codepoint = charString.codePointAt(0);
            double width = advances.get(charString).asDouble();
            fontInfo.setWidth(codepoint, width);
        });
             
        System.out.println("Processed space provider: " + advances.size() + " advance entries");
    }
}
