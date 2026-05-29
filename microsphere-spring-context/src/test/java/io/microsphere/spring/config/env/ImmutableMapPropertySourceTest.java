package io.microsphere.spring.config.env;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.collection.MapUtils.newTreeMap;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ImmutableMapPropertySource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ImmutableMapPropertySource
 * @since 1.0.0
 */
class ImmutableMapPropertySourceTest {

    private static final String NAME = "test";

    private static final Class<? extends Map> SOURCE_CLASS = unmodifiableMap(emptyMap()).getClass();

    @Test
    void testNewFromSortedMap() {
        ImmutableMapPropertySource propertySource = createPropertySource(newTreeMap());
        assertPropertySource(propertySource);
    }

    @Test
    void testNewFromLinkedHashMap() {
        ImmutableMapPropertySource propertySource = createPropertySource(newLinkedHashMap());
        assertPropertySource(propertySource);
    }

    @Test
    void testNewFromIdentityHashMap() {
        ImmutableMapPropertySource propertySource = createPropertySource(new IdentityHashMap<>());
        assertPropertySource(propertySource);
    }

    @Test
    void testNewFromHashMap() {
        ImmutableMapPropertySource propertySource = createPropertySource(newHashMap());
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