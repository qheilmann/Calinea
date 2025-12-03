package io.calinea.generator;

import io.calinea.generator.writer.JsonPackWriter;
import io.calinea.pack.PackInfo;
import io.calinea.pack.font.FontInfo;
import io.calinea.pack.font.FontsInfo;
import io.calinea.pack.reader.JsonPackReader;
import io.calinea.pack.translation.TranslationsInfo;
import net.kyori.adventure.key.Key;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
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
            
            assertEquals(0, font.getDirectWidth('a').getWidth());
            assertEquals(0, font.getDirectWidth('Z').getWidth());
            assertEquals(0, font.getDirectWidth('€').getWidth());
            assertEquals(0, font.getDirectWidth(0x1F600).getWidth());
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
            FontsInfo fontsInfo = new FontsInfo();
            fontsInfo.addFont(originalFont);
            PackInfo originalPackInfo = new PackInfo(fontsInfo, new TranslationsInfo());
            
            JsonPackWriter writer = new JsonPackWriter(originalPackInfo);
            Path jsonFile = tempDir.resolve("simple-font.json");
            writer.write(jsonFile);
            
            assertFileWritten(jsonFile);

            JsonPackReader reader = new JsonPackReader();
            PackInfo loadedPackInfo = reader.read(jsonFile);
            FontsInfo loadedFontsInfo = loadedPackInfo.fontsInfo();
            assertNotNull(loadedFontsInfo);
            assertEquals(1, loadedFontsInfo.getFonts().size());
            assertFontsEqual(originalFont, loadedFontsInfo.getFont(fontKey));
        }
        
        @Test
        void shouldWriteAndReadMultipleFonts(@TempDir Path tempDir) throws Exception {
            Key fontKey1 = key("font1");
            Key fontKey2 = key("font2");
            FontInfo font1 = createSampleFont(fontKey1);
            FontInfo font2 = createSampleFont(fontKey2);
            font2.setWidth('Z', 8); // Add different character override
            FontsInfo fontsInfo = new FontsInfo();
            fontsInfo.addFont(font1);
            fontsInfo.addFont(font2);

            PackInfo originalPackInfo = new PackInfo(fontsInfo, new TranslationsInfo());

            JsonPackWriter writer = new JsonPackWriter(originalPackInfo);
            Path jsonFile = tempDir.resolve("multiple-fonts.json");
            writer.write(jsonFile);

            assertFileWritten(jsonFile);

            JsonPackReader reader = new JsonPackReader();
            PackInfo loadedPackInfo = reader.read(jsonFile);
            FontsInfo loadedFontsInfo = loadedPackInfo.fontsInfo();
            assertNotNull(loadedFontsInfo);
            assertEquals(2, loadedFontsInfo.getFonts().size());

            assertFontsEqual(font1, loadedFontsInfo.getFont(fontKey1));
            assertFontsEqual(font2, loadedFontsInfo.getFont(fontKey2));
        }
        
        @Test
        void shouldHandleVariousCharacterTypes(@TempDir Path tempDir) throws Exception {
            Key fontKey = key("unicode_font");
            FontInfo font = createSampleFont(fontKey);
            FontsInfo originalFonts = new FontsInfo();
            originalFonts.addFont(font);
            PackInfo originalPackInfo = new PackInfo(originalFonts, new TranslationsInfo());
            
            JsonPackWriter writer = new JsonPackWriter(originalPackInfo);
            Path jsonFile = tempDir.resolve("unicode-font.json");
            writer.write(jsonFile);
            
            assertFileWritten(jsonFile);

            JsonPackReader reader = new JsonPackReader();
            PackInfo loadedPackInfo = reader.read(jsonFile);
            FontsInfo loadedFontsInfo = loadedPackInfo.fontsInfo();
            assertNotNull(loadedFontsInfo);
            assertEquals(1, loadedFontsInfo.getFonts().size());
            
            FontInfo loadedFont = loadedFontsInfo.getFont(fontKey);
            assertFontsEqual(font, loadedFont);
            
            // Verify specific character types
            assertEquals(8, loadedFont.getDirectWidth('A').getWidth());      // ASCII letter
            assertEquals(9, loadedFont.getDirectWidth('4').getWidth());      // ASCII digit  
            assertEquals(9, loadedFont.getDirectWidth('€').getWidth());      // Unicode symbol
            assertEquals(12, loadedFont.getDirectWidth(0x1F600).getWidth()); // Emoji
            assertEquals(0, loadedFont.getDirectWidth('C').getWidth());      // Unset character (default)
        }
        
        @Test
        void shouldHandleEmptyFont(@TempDir Path tempDir) throws Exception {
            Key emptyFontKey = key("empty_font");
            FontInfo emptyFont = new FontInfo(emptyFontKey);
            FontsInfo originalFonts = new FontsInfo();
            originalFonts.addFont(emptyFont);
            PackInfo originalPackInfo = new PackInfo(originalFonts, new TranslationsInfo());

            JsonPackWriter writer = new JsonPackWriter(originalPackInfo);
            Path jsonFile = tempDir.resolve("empty-font.json");
            writer.write(jsonFile);
            
            assertFileWritten(jsonFile);
            
            JsonPackReader reader = new JsonPackReader();
            PackInfo loadedPackInfo = reader.read(jsonFile);
            FontsInfo loadedFontsInfo = loadedPackInfo.fontsInfo();
            assertNotNull(loadedFontsInfo);
            assertEquals(1, loadedFontsInfo.getFonts().size());
            assertFontsEqual(emptyFont, loadedFontsInfo.getFont(emptyFontKey));
        }
    }
}