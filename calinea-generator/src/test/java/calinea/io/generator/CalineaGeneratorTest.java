package calinea.io.generator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.calinea.generator.CalineaGenerator;

class CalineaGeneratorTest {
    @Test void generatorClassShouldExist() {
        CalineaGenerator classUnderTest = new CalineaGenerator();
        assertNotNull(classUnderTest, "generator should be instantiable");
    }
}
