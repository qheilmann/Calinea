package io.calinea.generator.writer.sections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.calinea.pack.keybind.KeybindsInfo;

import java.util.Map;

/**
 * Writes the keybinds section containing keybind-to-display-text mappings.
 * <p>
 * Keybinds map Minecraft keybind keys (like "key.jump") to their text representations 
 * for fixed measuring purposes.
 * <p>
 * Output format:
 * <pre>
 * "keybinds": {
 *     "key.jump": "Space",
 *     "key.attack": "Left Button"
 * }
 * </pre>
 */
public class KeybindsSectionWriter implements ISectionWriter {
    
    private final KeybindsInfo keybindInfo;
    
    /**
     * Creates a new KeybindsSectionWriter.
     * 
     * @param keybinds map of keybind keys to their display text (e.g., "key.jump" -> "Space")
     */
    public KeybindsSectionWriter(KeybindsInfo keybinds) {
        this.keybindInfo = keybinds;
    }
    
    @Override
    public String getSectionName() {
        return "keybinds";
    }
    
    @Override
    public boolean hasData() {
        return keybindInfo != null && !keybindInfo.isEmpty();
    }
    
    @Override
    public void writeSection(ObjectNode root) {
        if (!hasData()) {
            return;
        }
        
        ObjectNode keybindsNode = root.putObject("keybinds");

        for (Map.Entry<String, String> entry : keybindInfo.keybinds().entrySet()) {
            keybindsNode.put(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public void printStatistics() {
        if (hasData()) {
            System.out.println("  - " + keybindInfo.size() + " keybinds");
        }
    }
}
