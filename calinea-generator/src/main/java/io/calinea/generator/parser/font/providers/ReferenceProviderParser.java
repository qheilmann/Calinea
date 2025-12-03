package io.calinea.generator.parser.font.providers;

import com.fasterxml.jackson.databind.JsonNode;

import io.calinea.generator.parser.PackPathContext;
import io.calinea.pack.font.FontInfo;
import net.kyori.adventure.key.Key;

import java.io.IOException;

/**
 * Parses reference font providers that point to other fonts.
 */
public class ReferenceProviderParser implements IProviderParser {
    
    @Override
    public String getType() {
        return "reference";
    }
    
    @Override
    public void parse(JsonNode provider, FontInfo fontInfo, PackPathContext context) throws IOException {
        String fontId = provider.get("id").asText();
        Key referencedFontKey = Key.key(fontId);
        
        fontInfo.addReference(referencedFontKey);

        System.out.println("Added font reference: " + fontId + " -> " + referencedFontKey);
    }
}
