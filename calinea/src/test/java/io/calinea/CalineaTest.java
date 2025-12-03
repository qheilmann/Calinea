package io.calinea;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.kyori.adventure.text.BlockNBTComponent.LocalPos;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.key.Key;
import io.calinea.config.CalineaGeneratorDefault;
import io.calinea.layout.Alignment;
import io.calinea.layout.LayoutContext;
import io.calinea.pack.PackInfo;
import io.calinea.resolver.ComponentResolver;
import io.calinea.segmentation.SegmentationResult;
import io.calinea.segmentation.splitter.TextTokenizer;

class CalineaTest {

    private static final double DELTA = 0.01;

    @BeforeAll
    static void setup() {
        CalineaTestHelper.setup();
    }

    @Nested
    class ClassicApi {
        @Test
        void testAlignLeft() {
            Component root = Component.text("Test");
            Component result = Calinea.alignLeft(root, 100);
            
            double rootWidth = Calinea.measure(root);
            double expectedWidth = rootWidth; // alignLeft should produce a component of the root's width

            double actualWidth = Calinea.measure(result);

            assertEquals(expectedWidth, actualWidth, DELTA, "alignLeft should produce a component of the expected width");
        }

        @Test
        void testCenter() {
            Component root = Component.text("Test");
            Component result = Calinea.center(root, 100);

            double rootWidth = Calinea.measure(root);
            double expectedWidth = 100/2 + rootWidth/2; // midpoint + half of root width

            double actualWidth = Calinea.measure(result);

            assertEquals(expectedWidth, actualWidth, DELTA, "center should produce a component which is centered within the specified width, so slightly more than half the width");
        }

        @Test
        void testAlignRight() {
            Component root = Component.text("Test");
            Component result = Calinea.alignRight(root, 100);
            
            double actualWidth = Calinea.measure(result);
            double expectedWidth = 100; // alignRight should produce full width

            assertEquals(expectedWidth, actualWidth, DELTA, "alignRight should produce a component of the specified width");
        }
    }

    @Nested
    class AdvancedApi {
        @Test
        void testPadding() {
            Component result = Calinea.layout(Component.text("Test"))
                .width(100)
                .align(Alignment.LEFT)
                .padding(10)
                .build();

            double actualPaddingWidth = Calinea.measure(result.children().getFirst());

            assertEquals(10, actualPaddingWidth, DELTA, "Padding width should be 10 pixels");
        }

        @Test
        void testFillLines() {
             Component result = Calinea.layout(Component.text("Test"))
                .width(100)
                .fillLines(true)
                .build();

            double width = Calinea.measure(result);
            assertEquals(100, width, DELTA,"fillLines should fill to the specified width");
        }
    }

    @Nested
    class Measure {
        @Test
        void testMeasureEmpty() {
            double width = Calinea.measure(Component.empty());
            assertEquals(0, width, DELTA, "Width of empty component should be 0");
        }

        @Test
        void testMeasureText() {
            double width = Calinea.measure(Component.text("Hello"));
            assertEquals(24, width, DELTA, "Width of 'Hello' should be 24");
        }

        void testMeasureTextWithSpace() {
            double width = Calinea.measure(Component.text("Hello !"));
            assertEquals(30, width, DELTA, "Width of 'Hello !' should be 30");
        }

        @Test
        void testMeasureNonResolvedScore() {
            double width = Calinea.measure(Component.score("player", "objective"));

            // Unresolved score must have 0 width
            assertEquals(0, width, DELTA, "Non-resolved score width should be 0");
        }
        
        @Test
        void testMeasureNonResolvedSelector() {
            String patternString = "@s";
            double width = Calinea.measure(Component.selector(patternString));
            double testWidth = Calinea.measure(Component.text(patternString));
            assertEquals(testWidth, width, DELTA, "Non-resolved selector width should match text width of the pattern");
        }
        
        /*
        We can't rely on paperserver translation during tests, so we can't do test without creating a custom measurer.

        @Test
        void testMeasureNonResolvedTranslatable() {
            String identifier = "chat.square_brackets";
            String englishTranslation = "[%s]";

            // Without any arguments -> "[%s]"
            double translatableWidth0arg = Calinea.measure(Component.translatable(identifier));
            double textWidth0arg = Calinea.measure(Component.text(String.format(englishTranslation, "")));
            assertEquals(textWidth0arg, translatableWidth0arg, DELTA, "Non-resolved translatable width should match the english translation width (0 args)");
            
            // With a single argument -> "[Hello]"
            double translatableWidth1arg = Calinea.measure(Component.translatable(identifier, Component.text("Hello")));
            double textWidth1arg = Calinea.measure(Component.text(String.format(englishTranslation, "Hello")));
            assertEquals(textWidth1arg, translatableWidth1arg, DELTA, "Non-resolved translatable width should match the english translation width (1 arg)");

            // With more than one argument -> "[Hello]" (only first used)
            double translatableWidth2arg = Calinea.measure(Component.translatable(identifier, Component.text("Hello"), Component.text("Ignored")));
            double textWidth2arg = Calinea.measure(Component.text(String.format(englishTranslation, "Hello")));
            assertEquals(textWidth2arg, translatableWidth2arg, DELTA, "Non-resolved translatable width should match the english translation width (2 args)");
        }
        */
        
        @Test
        void testMeasureKeybind() {
            String identifier = "key.jump";
            double width = Calinea.measure(Component.keybind(identifier));
            double testWidth = Calinea.measure(Component.text(identifier));
            assertEquals(testWidth, width, DELTA, "Non-resolved keybind width should match the identifier text width");
        }

        @Test
        void testMeasureNonResolvedBlockNBT() {
            double width = Calinea.measure(Component.blockNBT("{}", LocalPos.localPos(0, 0, 0)));
               
            // Unresolved block NBT must have 0 width
            assertEquals(0, width, DELTA, "Non-resolved BlockNBTComponent width should be 0");
        }

        @Test
        void testMeasureNonResolvedEntityNBT() {
            double width = Calinea.measure(Component.entityNBT("{}", "@s"));

            // Unresolved entity NBT must have 0 width
            assertEquals(0, width, DELTA, "Non-resolved EntityNBTComponent width should be 0");
        }

        @Test
        void testMeasureNonResolvedStorageNBT() {
            double width = Calinea.measure(Component.storageNBT("{}", Key.key("namespace:path")));

            // Unresolved storage NBT must have 0 width
            assertEquals(0, width, DELTA, "Non-resolved StorageNBTComponent width should be 0");
        }

        @Test
        void testMeasureObject() {
            // Sprite
            double spriteWidth = Calinea.measure(Component.object(ObjectContents.sprite(Key.key("block/diamond_block"))));
            assertEquals(8, spriteWidth, DELTA, "Width of any object sprite should be 8");

            // Player head
            double headWidth = Calinea.measure(Component.object(ObjectContents.playerHead("Notch")));
            assertEquals(8, headWidth, DELTA, "Width of any player head object should be 8");
        }

        @Test
        void testMeasureWithStyle() {
            String hello = "Hello";
            double widthWithoutBold = Calinea.measure(Component.text(hello));
            double width = Calinea.measure(Component.text(hello, Style.style(TextDecoration.BOLD)));
            double expectedWidth = widthWithoutBold + hello.length(); // Bold adds approx 1 per character
            // Bold text is usually wider than normal text
            assertEquals(expectedWidth, width, DELTA,"Bold text width should be increased by approx 1 per character");
        }

        @Test
        void testCustomFont() {
            double width = Calinea.measure(Component.text("Hello").font(Key.key("minecraft:alt")));
            assertEquals(25, width, DELTA, "Custom font width should be 25");
        }

        @Test
        void testMeasureWithHierarchy() {

            Component root = Component.text("Root");
            Component child = Component.text("Child", NamedTextColor.RED, TextDecoration.BOLD);
            Component grandChild = Component.text("GrandChild", Style.style(TextDecoration.ITALIC));
            Component otherChild = Component.text("OtherChild", NamedTextColor.BLUE);

            Component c = root
                .append(child.append(grandChild))
                .append(otherChild);
                
            double width = Calinea.measure(c);

            double expectedWidth = Calinea.measure(root) 
            + Calinea.measure(child.style(child.style().merge(root.style()))) 
            + Calinea.measure(grandChild.style(grandChild.style().merge(child.style()).merge(root.style())))
            + Calinea.measure(otherChild.style(otherChild.style().merge(root.style())));

            assertEquals(expectedWidth, width, DELTA, "Width with hierarchy should equal sum of individual parts with their parents' styles applied");
        }
    }

    @Nested
    class Split {
        @Test
        void testSplit() {
            Component c = Component.text("Long text that should be split into multiple lines because it exceeds the width");
            SegmentationResult result = Calinea.split(c, 50);
            assertFalse(result.lines().isEmpty(), "Split result should have lines");
            assertTrue(result.lines().size() > 1, "Should be split into multiple lines");
            assertTrue(result.lines().get(0).width() <= 50, "First line width should less than or equal to 50");
        }
    }

    @Nested
    class Context {
        @Test
        void testDifferentContext() {

            Path fontPath = Paths.get("calinea/src/test/resources/alternative-" + CalineaGeneratorDefault.DEFAULT_OUTPUT_FILENAME);
            if (!fontPath.toFile().exists()) {
                // Fallback for when running in some IDE configurations or CI
                fontPath = Paths.get("src/test/resources/alternative-" + CalineaGeneratorDefault.DEFAULT_OUTPUT_FILENAME);
            }
            
            if (!fontPath.toFile().exists()) {
                throw new RuntimeException("Could not find alternative-" + CalineaGeneratorDefault.DEFAULT_OUTPUT_FILENAME + " at " + fontPath.toAbsolutePath());
            }

            // Create a new context manually
            PackInfo packInfo = Calinea.createPackInfo(fontPath);
            LayoutContext context = Calinea.createContext(packInfo)
                .textTokenizer(new TextTokenizer() {
                    @Override
                    public List<String> tokenize(String text) {
                        return List.of(text.split("(?<=-)|(?=-)"));
                    }
                })
                .componentResolver(new ComponentResolver(packInfo))
                .build();
                
            // Use the context in a builder
            Component result = Calinea.layout(Component.text("AABC")) // 5 5 10 15
                .layoutContext(context)
                .width(10)
                .align(Alignment.LEFT)
                .build();

            // Verify the number of line breaks
            long numberBreak = result.children().stream()
                .filter(Predicate.isEqual(Component.newline()))
                .count();
            assertEquals(2, numberBreak, "There should be 2 line breaks (3 lines) in this custom context");

            // Verify each line's width
            List<Component> children = result.children();

            double firstLineWidth = context.componentMeasurer().measure(children.get(0));
            Calinea.logger().info("First line: " + children.get(0).toString());
            assertEquals(10, firstLineWidth, DELTA, "First line width should be 10");

            double secondLineWidth = context.componentMeasurer().measure(children.get(2)); // index 2 due to newline at index 1
            Calinea.logger().info("Second line: " + children.get(2).toString());
            assertEquals(10, secondLineWidth, DELTA, "Second line width should be 10");

            double thirdLineWidth = context.componentMeasurer().measure(children.get(4)); // index 4 due to newlines at index 1 and 3
            Calinea.logger().info("Third line: " + children.get(4).toString());
            assertEquals(15, thirdLineWidth, DELTA, "Third line width should be 15");
        }
    }

    @Nested
    class Separator {
        @Test
        void testSeparator() {
            Component sep = Calinea.separator(Component.text("-"), 100, true);

            double width = Calinea.measure(sep); // repeatToFill so should fill until 100
            assertEquals(100, width, DELTA, "Separator width should be 100");
        }
    }
}
