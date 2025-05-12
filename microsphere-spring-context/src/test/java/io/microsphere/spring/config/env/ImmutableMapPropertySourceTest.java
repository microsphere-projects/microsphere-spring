package io.microsphere.spring.config.env;

import org.junit.Test;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static org.junit.Assert.assertEquals;

/**
 * {@link ImmutableMapPropertySource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ImmutableMapPropertySource
 * @since 1.0.0
 */
public class ImmutableMapPropertySourceTest {

    private static final String NAME = "test";

    private static final Class<? extends Map> SOURCE_CLASS = unmodifiableMap(emptyMap()).getClass();

    @Test
    public void testNewFromSortedMap() {
        ImmutableMapPropertySource propertySource = createPropertySource(new TreeMap<>());
        assertPropertySource(propertySource);
    }

    @Test
    public void testNewFromLinkedHashMap() {
        ImmutableMapPropertySource propertySource = createPropertySource(new LinkedHashMap<>());
        assertPropertySource(propertySource);
    }

    @Test
    public void testNewFromIdentityHashMap() {
        ImmutableMapPropertySource propertySource = createPropertySource(new IdentityHashMap<>());
        assertPropertySource(propertySource);
    }

    @Test
    public void testNewFromHashMap() {
        ImmutableMapPropertySource propertySource = createPropertySource(new HashMap<>());
        assertPropertySource(propertySource);
    }

    void assertPropertySource(ImmutableMapPropertySource propertySource) {
        assertEquals(NAME, propertySource.getName());
        assertEquals(SOURCE_CLASS, propertySource.getSource().getClass());
    }

    private ImmutableMapPropertySource createPropertySource(Map source) {
        return new ImmutableMapPropertySource(NAME, source);
    }
}