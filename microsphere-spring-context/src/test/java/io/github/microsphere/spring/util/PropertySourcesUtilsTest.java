package io.github.microsphere.spring.util;

import org.junit.Test;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockEnvironment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.github.microsphere.spring.util.PropertySourcesUtils.DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME;
import static io.github.microsphere.spring.util.PropertySourcesUtils.addDefaultProperties;
import static io.github.microsphere.spring.util.PropertySourcesUtils.getDefaultProperties;
import static io.github.microsphere.spring.util.PropertySourcesUtils.getDefaultPropertiesPropertySource;
import static io.github.microsphere.spring.util.PropertySourcesUtils.getSubProperties;
import static org.junit.Assert.assertEquals;

/**
 * {@link PropertySourcesUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourcesUtils
 * @since 2017.01.13
 */
@SuppressWarnings("unchecked")
public class PropertySourcesUtilsTest {

    @Test
    public void testGetSubProperties() {

        ConfigurableEnvironment environment = new AbstractEnvironment() {
        };

        MutablePropertySources propertySources = environment.getPropertySources();

        Map<String, Object> source = new HashMap<String, Object>();
        Map<String, Object> source2 = new HashMap<String, Object>();

        MapPropertySource propertySource = new MapPropertySource("propertySource", source);
        MapPropertySource propertySource2 = new MapPropertySource("propertySource2", source2);

        propertySources.addLast(propertySource);
        propertySources.addLast(propertySource2);

        Map<String, Object> result = getSubProperties(propertySources, "user");

        assertEquals(Collections.emptyMap(), result);

        source.put("age", "31");
        source.put("user.name", "Mercy");
        source.put("user.age", "${age}");

        source2.put("user.name", "mercyblitz");
        source2.put("user.age", "32");

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", "Mercy");
        expected.put("age", "31");

        assertEquals(expected, getSubProperties((Iterable) propertySources, "user"));

        assertEquals(expected, getSubProperties(environment, "user"));

        assertEquals(expected, getSubProperties(propertySources, "user"));

        assertEquals(expected, getSubProperties(propertySources, environment, "user"));

        result = getSubProperties(propertySources, "");

        assertEquals(Collections.emptyMap(), result);

        result = getSubProperties(propertySources, "no-exists");

        assertEquals(Collections.emptyMap(), result);

    }

    @Test
    public void testDefaultProperties() {
        MockEnvironment environment = new MockEnvironment();
        addDefaultProperties(environment, "key-1", "value-1", "key-2", "value-2");

        assertEquals("value-1", environment.getProperty("key-1"));
        assertEquals("value-2", environment.getProperty("key-2"));

        MapPropertySource mapPropertySource = getDefaultPropertiesPropertySource(environment, false);
        assertEquals(DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME, mapPropertySource.getName());
        assertEquals("defaultProperties", mapPropertySource.getName());

        Map<String, Object> defaultProperties = getDefaultProperties(environment, false);
        assertEquals(2, defaultProperties.size());
        assertEquals("value-1", defaultProperties.get("key-1"));
        assertEquals("value-2", defaultProperties.get("key-2"));
    }

}
