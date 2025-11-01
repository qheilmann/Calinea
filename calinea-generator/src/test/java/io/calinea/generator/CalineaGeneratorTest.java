package io.calinea.generator;

import io.calinea.generator.model.FontInfo;
import io.calinea.generator.writer.JsonFontWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalineaGeneratorTest {
    
    @Test
    public void testFontInfoBasicOperations() {
        FontInfo font = new FontInfo("test", 6);
        
        assertEquals("test", font.getName());
        assertEquals(6, font.getDefaultWidth());
        assertEquals(6, font.getWidth('a')); // Default width
        
        font.setWidth('A', 8);
        assertEquals(8, font.getWidth('A'));
        assertEquals(1, font.getNonDefaultWidths().size());
        
        // Setting to default width should remove override
        font.setWidth('A', 6);
        assertEquals(0, font.getNonDefaultWidths().size());
    }
    
    @Test
    public void testJsonWriteAndRead(@TempDir Path tempDir) throws Exception {
        // Create test fonts
        FontInfo font1 = new FontInfo("test_font", 6);
        font1.setWidth('A', 8);        // \u0041
        font1.setWidth('B', 7);        // \u0042
        font1.setWidth('€', 9);        // \u20AC (Euro symbol)
        font1.setWidth(0x1F600, 12);   // \uD83D\uDE00 (Emoji - surrogate pair)
        
        FontInfo font2 = new FontInfo("alt_font", 5);
        font2.setWidth('A', 6);
        font2.setWidth('Z', 8);
        
        List<FontInfo> originalFonts = Arrays.asList(font1, font2);
        
        // Write to JSON format
        JsonFontWriter writer = new JsonFontWriter();
        Path jsonFile = tempDir.resolve("font-widths.json");
        writer.writeFonts(originalFonts, jsonFile);
        
        assertTrue(jsonFile.toFile().exists());
        assertTrue(jsonFile.toFile().length() > 0);
        
        // Read back
        List<FontInfo> loadedFonts = writer.readFonts(jsonFile);
        
        assertEquals(2, loadedFonts.size());
        
        FontInfo loadedFont1 = loadedFonts.get(0);
        assertEquals(font1.getName(), loadedFont1.getName());
        assertEquals(font1.getDefaultWidth(), loadedFont1.getDefaultWidth());
        assertEquals(font1.getWidth('A'), loadedFont1.getWidth('A'));
        assertEquals(font1.getWidth('B'), loadedFont1.getWidth('B'));
        assertEquals(font1.getWidth('€'), loadedFont1.getWidth('€'));
        assertEquals(font1.getWidth(0x1F600), loadedFont1.getWidth(0x1F600));
        assertEquals(font1.getWidth('C'), loadedFont1.getWidth('C')); // Default width
        
        FontInfo loadedFont2 = loadedFonts.get(1);
        assertEquals(font2.getName(), loadedFont2.getName());
        assertEquals(font2.getDefaultWidth(), loadedFont2.getDefaultWidth());
        assertEquals(font2.getWidth('A'), loadedFont2.getWidth('A'));
        assertEquals(font2.getWidth('Z'), loadedFont2.getWidth('Z'));
    }
    
    @Test
    public void testMultipleFontsGeneration(@TempDir Path tempDir) throws Exception {
        // Create multiple test fonts
        FontInfo font1 = new FontInfo("default", 6);
        font1.setWidth('A', 8);
        font1.setWidth('B', 7);
        
        FontInfo font2 = new FontInfo("alt", 5);
        font2.setWidth('A', 6);
        font2.setWidth('€', 8);
        
        List<FontInfo> fonts = Arrays.asList(font1, font2);
        
        // Generate JSON file
        JsonFontWriter writer = new JsonFontWriter();
        Path jsonFile = tempDir.resolve("font-widths.json");
        writer.writeFonts(fonts, jsonFile);
        
        // Check generated file
        assertTrue(jsonFile.toFile().exists());
        assertTrue(jsonFile.toFile().length() > 0);
        
        // Verify content by reading back
        List<FontInfo> loadedFonts = writer.readFonts(jsonFile);
        assertEquals(2, loadedFonts.size());
        
        FontInfo loadedFont1 = loadedFonts.stream()
            .filter(f -> f.getName().equals("default"))
            .findFirst().orElseThrow();
        assertEquals(6, loadedFont1.getDefaultWidth());
        assertEquals(8, loadedFont1.getWidth('A'));
        
        FontInfo loadedFont2 = loadedFonts.stream()
            .filter(f -> f.getName().equals("alt"))
            .findFirst().orElseThrow();
        assertEquals(5, loadedFont2.getDefaultWidth());
        assertEquals(6, loadedFont2.getWidth('A'));
        assertEquals(8, loadedFont2.getWidth('€'));
    }
}