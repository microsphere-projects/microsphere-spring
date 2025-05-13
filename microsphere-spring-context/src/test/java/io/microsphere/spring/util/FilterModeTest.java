package io.microsphere.spring.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static io.microsphere.spring.util.FilterMode.CONDITIONAL;
import static io.microsphere.spring.util.FilterMode.PROPERTY_NAME;
import static io.microsphere.spring.util.FilterMode.SEQUENTIAL;
import static io.microsphere.spring.util.FilterMode.valueOf;
import static io.microsphere.spring.util.FilterMode.values;
import static io.microsphere.util.ArrayUtils.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FilterMode} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see FilterMode
 * @since 1.0.0
 */
public class FilterModeTest {

    @Test
    public void testConstants() {
        assertEquals("microsphere.spring.filter-mode", PROPERTY_NAME);
    }

    @Test
    public void testValueOf() {
        assertEquals(SEQUENTIAL, valueOf("SEQUENTIAL"));
        assertEquals(CONDITIONAL, valueOf("CONDITIONAL"));
    }

    @Test
    public void testValueOfFromEnvironment() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty(PROPERTY_NAME, "SEQUENTIAL");

        assertEquals(SEQUENTIAL, valueOf(mockEnvironment));

        mockEnvironment.setProperty(PROPERTY_NAME, "CONDITIONAL");
        assertEquals(CONDITIONAL, valueOf(mockEnvironment));
    }

    @Test
    public void testValues() {
        FilterMode[] values = values();
        assertTrue(contains(values, SEQUENTIAL));
        assertTrue(contains(values, CONDITIONAL));
    }
}