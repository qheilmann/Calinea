package io.calinea;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import net.kyori.adventure.text.Component;

class CalineaTest {
    
    @Test void centerTextShouldWork() {
        Component result = Calinea.center("Test");
        assertNotNull(result, "center should return a component");
    }
    
    @Test void measureWidthShouldReturnPositiveValue() {
        double width = Calinea.measureWidth("Hello");
        assertTrue(width > 0, "width should be positive");
    }
    
    @Test void alignLeftShouldWork() {
        Component result = Calinea.alignLeft("Test", 100);
        assertNotNull(result, "alignLeft should return a component");
    }
    
    @Test void alignRightShouldWork() {
        Component result = Calinea.alignRight("Test", 100);
        assertNotNull(result, "alignRight should return a component");
    }
    
    @Test void separatorShouldWork() {
        Component result = Calinea.separator(100);
        assertNotNull(result, "separator should return a component");
    }
}
