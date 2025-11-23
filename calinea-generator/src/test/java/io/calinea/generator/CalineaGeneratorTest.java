package io.calinea.generator;

import io.calinea.font.FontInfo;
import io.calinea.font.PackInfo;
import io.calinea.font.reader.JsonFontReader;
import io.calinea.generator.writer.JsonFontWriter;
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
    
    private FontInfo createSampleFont(Key name) {
        FontInfo font = new FontInfo(name);
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
    
    private void assertFontsEqual(FontInfo expected, FontInfo actual) {
        assertEquals(expected.getFontKey(), actual.getFontKey(), "Font names should match");
        assertEquals(expected.getWidths().size(), actual.getWidths().size(), 
                    "Number of character overrides should match");
        
        // Check each character width
        for (var entry : expected.getWidths().entrySet()) {
            int codepoint = entry.getKey();
            double expectedWidth = entry.getValue();
            double actualWidth = actual.getDirectWidth(codepoint).getWidth();
            assertEquals(expectedWidth, actualWidth, 
                        "Width for codepoint U+" + String.format("%04X", codepoint) + " should match");
        }
    }

    private Key key(String name) {
        return Key.key("test", name);
    }
    
    @Nested
    class FontInfoModelTests {
        
        @Test
        void shouldCreateFontWithCorrectProperties() {
            Key fontKey = key("test_font");
            FontInfo font = new FontInfo(fontKey);
            
            assertEquals(fontKey, font.getFontKey());
        }
        
        @Test
        void shouldReturnErrorCodeForUnsetCharacters() {
            FontInfo font = new FontInfo(key("test_font"));
            
            assertEquals(-1, font.getDirectWidth('a').getWidth());
            assertEquals(-1, font.getDirectWidth('Z').getWidth());
            assertEquals(-1, font.getDirectWidth('€').getWidth());
            assertEquals(-1, font.getDirectWidth(0x1F600).getWidth());
        }
        
        @Test
        void shouldSetAndGetCharacterWidths() {
            FontInfo font = new FontInfo(key("test_font"));
            
            font.setWidth('A', 8);
            assertEquals(8, font.getDirectWidth('A').getWidth());
            
            font.setWidth('B', 7);
            assertEquals(7, font.getDirectWidth('B').getWidth());
        }
        
        @Test
        void shouldHandleUnicodeCharactersAndSurrogatePairs() {
            FontInfo font = new FontInfo(key("test_font"));

            // Unicode symbol
            font.setWidth('€', 9);
            assertEquals(9, font.getDirectWidth('€').getWidth());
            
            // Emoji (surrogate pair)
            font.setWidth(0x1F600, 12);
            assertEquals(12, font.getDirectWidth(0x1F600).getWidth());
            
            assertEquals(2, font.getWidths().size());
        }
    }
    
    @Nested
    class JsonSerializationTests {
        
        @Test
        void shouldWriteAndReadSimpleFont(@TempDir Path tempDir) throws Exception {
            Key fontKey = key("simple_font");
            FontInfo originalFont = createSampleFont(fontKey);
            List<FontInfo> originalFonts = Arrays.asList(originalFont);
            PackInfo originalPackInfo = new PackInfo(originalFonts);
            
            JsonFontWriter writer = new JsonFontWriter();
            Path jsonFile = tempDir.resolve("simple-font.json");
            writer.writePackInfo(originalPackInfo, jsonFile);
            
            assertFileWritten(jsonFile);

            JsonFontReader reader = new JsonFontReader();
            PackInfo loadedPackInfo = reader.readFonts(jsonFile);
            assertEquals(1, loadedPackInfo.getFonts().size());
            assertFontsEqual(originalFont, loadedPackInfo.getFont(fontKey));
        }
        
        @Test
        void shouldWriteAndReadMultipleFonts(@TempDir Path tempDir) throws Exception {
            Key fontKey1 = key("font1");
            Key fontKey2 = key("font2");
            FontInfo font1 = createSampleFont(fontKey1);
            FontInfo font2 = createSampleFont(fontKey2);
            font2.setWidth('Z', 8); // Add different character override
            
            List<FontInfo> originalFonts = Arrays.asList(font1, font2);
            PackInfo originalPackInfo = new PackInfo(originalFonts);
            
            JsonFontWriter writer = new JsonFontWriter();
            Path jsonFile = tempDir.resolve("multiple-fonts.json");
            writer.writePackInfo(originalPackInfo, jsonFile);

            assertFileWritten(jsonFile);

            JsonFontReader reader = new JsonFontReader();
            PackInfo loadedPackInfo = reader.readFonts(jsonFile);
            assertEquals(2, loadedPackInfo.getFonts().size());

            assertFontsEqual(font1, loadedPackInfo.getFont(fontKey1));
            assertFontsEqual(font2, loadedPackInfo.getFont(fontKey2));
        }
        
        @Test
        void shouldHandleVariousCharacterTypes(@TempDir Path tempDir) throws Exception {
            Key fontKey = key("unicode_font");
            FontInfo font = createSampleFont(fontKey);
            List<FontInfo> originalFonts = Arrays.asList(font);
            PackInfo originalPackInfo = new PackInfo(originalFonts);
            
            JsonFontWriter writer = new JsonFontWriter();
            Path jsonFile = tempDir.resolve("unicode-font.json");
            writer.writePackInfo(originalPackInfo, jsonFile);
            
            assertFileWritten(jsonFile);

            JsonFontReader reader = new JsonFontReader();
            PackInfo loadedPackInfo = reader.readFonts(jsonFile);
            assertEquals(1, loadedPackInfo.getFonts().size());
            
            FontInfo loadedFont = loadedPackInfo.getFont(fontKey);
            assertFontsEqual(font, loadedFont);
            
            // Verify specific character types
            assertEquals(8, loadedFont.getDirectWidth('A').getWidth());      // ASCII letter
            assertEquals(9, loadedFont.getDirectWidth('4').getWidth());      // ASCII digit  
            assertEquals(9, loadedFont.getDirectWidth('€').getWidth());      // Unicode symbol
            assertEquals(12, loadedFont.getDirectWidth(0x1F600).getWidth()); // Emoji
            assertEquals(-1, loadedFont.getDirectWidth('C').getWidth());      // Unset character (default)
        }
        
        @Test
        void shouldHandleEmptyFont(@TempDir Path tempDir) throws Exception {
            Key emptyFontKey = key("empty_font");
            FontInfo emptyFont = new FontInfo(emptyFontKey);
            List<FontInfo> originalFonts = Arrays.asList(emptyFont);
            PackInfo originalPackInfo = new PackInfo(originalFonts);
            
            JsonFontWriter writer = new JsonFontWriter();
            Path jsonFile = tempDir.resolve("empty-font.json");
            writer.writePackInfo(originalPackInfo, jsonFile);
            
            assertFileWritten(jsonFile);
            
            JsonFontReader reader = new JsonFontReader();
            PackInfo loadedPackInfo = reader.readFonts(jsonFile);
            assertEquals(1, loadedPackInfo.getFonts().size());
            assertFontsEqual(emptyFont, loadedPackInfo.getFont(emptyFontKey));
        }
    }
}