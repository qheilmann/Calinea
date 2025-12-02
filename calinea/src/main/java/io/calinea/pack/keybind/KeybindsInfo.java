package io.calinea.pack.keybind;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

/**
 * Contains keybind mappings from keybind identifiers to their display names.
 * For example: "key.jump" -> "Space"
 */
public class KeybindsInfo {
    
    private final Map<String, String> keybinds;
    
    public KeybindsInfo() {
        this.keybinds = new LinkedHashMap<>();
    }
    
    public KeybindsInfo(Map<String, String> keybinds) {
        this.keybinds = new LinkedHashMap<>(keybinds);
    }
    
    public void addKeybind(String key, String displayName) {
        keybinds.put(key, displayName);
    }
    
    public @Nullable String getDisplayName(String key) {
        return keybinds.get(key);
    }
    
    @Unmodifiable
    public Map<String, String> keybinds() {
        return Collections.unmodifiableMap(keybinds);
    }
    
    public boolean isEmpty() {
        return keybinds.isEmpty();
    }
    
    public int size() {
        return keybinds.size();
    }
    
    @Override
    public String toString() {
        return String.format("KeybindsInfo{count=%d}", keybinds.size());
    }
}
