package io.calinea.generator;

import io.calinea.generator.writer.JsonFontWriter;
import io.calinea.models.FontInfo;
import io.calinea.reader.JsonFontReader;
import net.kyori.adventure.key.Key;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalineaGeneratorTest {
    
    // Helper methods
    
    private FontInfo createSampleFont(Key name, int defaultWidth) {
        FontInfo font = new FontInfo(name, defaultWidth);
        font.setWidth('A', 8);        // ASCII letter
        font.setWidth('4', 9);        // ASCII digit
        font.setWidth('€', 9);        // Unicode symbol (Euro)
        font.setWidth(0x1F600, 12);   // Emoji (surrogate pair)
        return font;
    }
    
    private void assertFileWritten(Path file) {
        assertTrue(file.toFile().exists(), "File should exist");
        assertTrue(file.toFile().length() > 0, "File should not be empty");
    }
    
    private FontInfo findFontByName(List<FontInfo> fonts, Key fontKey) {
        return fonts.stream()
            .filter(f -> f.getFontKey().equals(fontKey))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Font '" + fontKey + "' not found"));
    }
    
    private void assertFontsEqual(FontInfo expected, FontInfo actual) {
        assertEquals(expected.getFontKey(), actual.getFontKey(), "Font names should match");
        assertEquals(expected.getDefaultWidth(), actual.getDefaultWidth(), "Default widths should match");
        assertEquals(expected.getNonDefaultWidths().size(), actual.getNonDefaultWidths().size(), 
                    "Number of character overrides should match");
        
        // Check each character width override
        for (var entry : expected.getNonDefaultWidths().entrySet()) {
            int codepoint = entry.getKey();
            int expectedWidth = entry.getValue();
            int actualWidth = actual.getWidth(codepoint);
            assertEquals(expectedWidth, actualWidth, 
                        "Width for codepoint U+" + String.format("%04X", codepoint) + " should match");
        }
    }

    @SuppressWarnings("null")
    private Key key(String name) {
        return Key.key("test", name);
    }
    
    @Nested
    class FontInfoModelTests {
        
        @Test
        void shouldCreateFontWithCorrectProperties() {
            Key fontKey = key("test_font");
            FontInfo font = new FontInfo(fontKey, 6);
            
            assertEquals(fontKey, font.getFontKey());
            assertEquals(6, font.getDefaultWidth());
            assertEquals(0, font.getNonDefaultWidths().size());
        }
        
        @Test
        void shouldReturnDefaultWidthForUnsetCharacters() {
            FontInfo font = new FontInfo(key("test_font"), 6);
            
            assertEquals(6, font.getWidth('a'));
            assertEquals(6, font.getWidth('Z'));
            assertEquals(6, font.getWidth('€'));
            assertEquals(6, font.getWidth(0x1F600));
        }
        
        @Test
        void shouldSetAndGetCharacterWidths() {
            FontInfo font = new FontInfo(key("test_font"), 6);
            
            font.setWidth('A', 8);
            assertEquals(8, font.getWidth('A'));
            assertEquals(1, font.getNonDefaultWidths().size());
            
            font.setWidth('B', 7);
            assertEquals(7, font.getWidth('B'));
            assertEquals(2, font.getNonDefaultWidths().size());
        }
        
        @Test
        void shouldRemoveOverrideWhenSettingToDefaultWidth() {
            FontInfo font = new FontInfo(key("test_font"), 6);
            
            font.setWidth('A', 8);
            assertEquals(1, font.getNonDefaultWidths().size());
            
            font.setWidth('A', 6); // Set back to default
            assertEquals(0, font.getNonDefaultWidths().size());
            assertEquals(6, font.getWidth('A'));
        }
        
        @Test
        void shouldHandleUnicodeCharactersAndSurrogatePairs() {
            FontInfo font = new FontInfo(key("test_font"), 6);
            
            // Unicode symbol
            font.setWidth('€', 9);
            assertEquals(9, font.getWidth('€'));
            
            // Emoji (surrogate pair)
            font.setWidth(0x1F600, 12);
            assertEquals(12, font.getWidth(0x1F600));
            
            assertEquals(2, font.getNonDefaultWidths().size());
        }
    }
    
    @Nested
    class JsonSerializationTests {
        
        @Test
        void shouldWriteAndReadSimpleFont(@TempDir Path tempDir) throws Exception {
            FontInfo originalFont = createSampleFont(key("simple_font"), 5);
            List<FontInfo> originalFonts = Arrays.asList(originalFont);
            
            JsonFontWriter writer = new JsonFontWriter();
            Path jsonFile = tempDir.resolve("simple-font.json");
            writer.writeFonts(originalFonts, jsonFile);
            
            assertFileWritten(jsonFile);

            JsonFontReader reader = new JsonFontReader();
            List<FontInfo> loadedFonts = reader.readFonts(jsonFile);
            assertEquals(1, loadedFonts.size());
            assertFontsEqual(originalFont, loadedFonts.get(0));
        }
        
        @Test
        void shouldWriteAndReadMultipleFonts(@TempDir Path tempDir) throws Exception {
            FontInfo font1 = createSampleFont(key("font1"), 6);
            FontInfo font2 = createSampleFont(key("font2"), 5);
            font2.setWidth('Z', 8); // Add different character override
            
            List<FontInfo> originalFonts = Arrays.asList(font1, font2);
            
            JsonFontWriter writer = new JsonFontWriter();
            Path jsonFile = tempDir.resolve("multiple-fonts.json");
            writer.writeFonts(originalFonts, jsonFile);
            
            assertFileWritten(jsonFile);

            JsonFontReader reader = new JsonFontReader();
            List<FontInfo> loadedFonts = reader.readFonts(jsonFile);
            assertEquals(2, loadedFonts.size());

            FontInfo loadedFont1 = findFontByName(loadedFonts, key("font1"));
            FontInfo loadedFont2 = findFontByName(loadedFonts, key("font2"));

            assertFontsEqual(font1, loadedFont1);
            assertFontsEqual(font2, loadedFont2);
        }
        
        @Test
        void shouldHandleVariousCharacterTypes(@TempDir Path tempDir) throws Exception {
            FontInfo font = createSampleFont(key("unicode_font"), 6);
            List<FontInfo> originalFonts = Arrays.asList(font);
            
            JsonFontWriter writer = new JsonFontWriter();
            Path jsonFile = tempDir.resolve("unicode-font.json");
            writer.writeFonts(originalFonts, jsonFile);
            
            assertFileWritten(jsonFile);

            JsonFontReader reader = new JsonFontReader();
            List<FontInfo> loadedFonts = reader.readFonts(jsonFile);
            assertEquals(1, loadedFonts.size());
            
            FontInfo loadedFont = loadedFonts.get(0);
            assertFontsEqual(font, loadedFont);
            
            // Verify specific character types
            assertEquals(8, loadedFont.getWidth('A'));      // ASCII letter
            assertEquals(9, loadedFont.getWidth('4'));      // ASCII digit  
            assertEquals(9, loadedFont.getWidth('€'));      // Unicode symbol
            assertEquals(12, loadedFont.getWidth(0x1F600)); // Emoji
            assertEquals(6, loadedFont.getWidth('C'));      // Unset character (default)
        }
        
        @Test
        void shouldHandleEmptyFont(@TempDir Path tempDir) throws Exception {
            FontInfo emptyFont = new FontInfo(key("empty_font"), 7);
            List<FontInfo> originalFonts = Arrays.asList(emptyFont);
            
            JsonFontWriter writer = new JsonFontWriter();
            Path jsonFile = tempDir.resolve("empty-font.json");
            writer.writeFonts(originalFonts, jsonFile);
            
            assertFileWritten(jsonFile);
            
            JsonFontReader reader = new JsonFontReader();
            List<FontInfo> loadedFonts = reader.readFonts(jsonFile);
            assertEquals(1, loadedFonts.size());
            assertFontsEqual(emptyFont, loadedFonts.get(0));
        }
    }
}