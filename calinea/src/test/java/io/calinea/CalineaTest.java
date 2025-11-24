package io.calinea;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import net.kyori.adventure.text.Component;

class CalineaTest {
    
    @Test void centerTextShouldWork() {
        Component result = Calinea.center(Component.text("Test"), 320);
        assertNotNull(result, "center should return a component");
    }
    
    @Test void measureWidthShouldReturnPositiveValue() {
        double width = Calinea.measure(Component.text("Hello"));
        assertTrue(width > 0, "width should be positive");
    }
}
