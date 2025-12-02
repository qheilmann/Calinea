package io.calinea.pack.reader;

import com.fasterxml.jackson.databind.JsonNode;

import io.calinea.pack.keybind.KeybindsInfo;

import java.io.IOException;

/**
 * Reads the "keybinds" section from Calinea config JSON.
 * Format is simple key-value pairs: { "key.jump": "Space", "key.sneak": "Shift" }
 */
public class KeybindsSectionReader implements ISectionReader<KeybindsInfo> {
    
    private static final String SECTION_NAME = "keybinds";
    
    @Override
    public String getSectionName() {
        return SECTION_NAME;
    }
    
    @Override
    public KeybindsInfo read(JsonNode keybindsNode) throws IOException {
        if (!keybindsNode.isObject()) {
            throw new IOException("'keybinds' section must be an object");
        }
        
        KeybindsInfo keybindsInfo = new KeybindsInfo();
        
        keybindsNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            String displayName = entry.getValue().asText();
            keybindsInfo.addKeybind(key, displayName);
        });
        
        return keybindsInfo;
    }
}
