package io.microsphere.spring.util;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.env.MockPropertySource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.spring.util.PropertySourcesUtils.DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME;
import static io.microsphere.spring.util.PropertySourcesUtils.addDefaultProperties;
import static io.microsphere.spring.util.PropertySourcesUtils.findConfiguredPropertySource;
import static io.microsphere.spring.util.PropertySourcesUtils.findConfiguredPropertySourceName;
import static io.microsphere.spring.util.PropertySourcesUtils.findPropertyNamesByPrefix;
import static io.microsphere.spring.util.PropertySourcesUtils.getConversionService;
import static io.microsphere.spring.util.PropertySourcesUtils.getDefaultProperties;
import static io.microsphere.spring.util.PropertySourcesUtils.getDefaultPropertiesPropertySource;
import static io.microsphere.spring.util.PropertySourcesUtils.getMapPropertySource;
import static io.microsphere.spring.util.PropertySourcesUtils.getPropertySource;
import static io.microsphere.spring.util.PropertySourcesUtils.getSubProperties;
import static io.microsphere.spring.util.PropertySourcesUtils.resolveCommaDelimitedValueToList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.core.convert.support.DefaultConversionService.getSharedInstance;

/**
 * {@link PropertySourcesUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourcesUtils
 * @since 2017.01.13
 */
@SuppressWarnings("unchecked")
public class PropertySourcesUtilsTest {

    private ConfigurableEnvironment environment;

    @Before
    public void before() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("test-key", "test-value");
        mockEnvironment.setProperty("test-key2", "test-value2");
        mockEnvironment.setConversionService((ConfigurableConversionService) getSharedInstance());
        this.environment = mockEnvironment;
    }

    @Test
    public void testGetPropertySource() {
        PropertySource propertySource = getPropertySource(environment, MockPropertySource.MOCK_PROPERTIES_PROPERTY_SOURCE_NAME, PropertySource.class);
        assertNotNull(propertySource);

        propertySource = getPropertySource(environment, MockPropertySource.MOCK_PROPERTIES_PROPERTY_SOURCE_NAME, EnumerablePropertySource.class);
        assertNotNull(propertySource);

        propertySource = getPropertySource(environment, MockPropertySource.MOCK_PROPERTIES_PROPERTY_SOURCE_NAME, MapPropertySource.class);
        assertNotNull(propertySource);

        propertySource = getPropertySource(environment, MockPropertySource.MOCK_PROPERTIES_PROPERTY_SOURCE_NAME, PropertiesPropertySource.class);
        assertNotNull(propertySource);

        propertySource = getPropertySource(environment, MockPropertySource.MOCK_PROPERTIES_PROPERTY_SOURCE_NAME, MockPropertySource.class);
        assertNotNull(propertySource);

        propertySource = getPropertySource(environment, MockPropertySource.MOCK_PROPERTIES_PROPERTY_SOURCE_NAME, ResourcePropertySource.class);
        assertNull(propertySource);

        propertySource = getPropertySource(environment, "test", ResourcePropertySource.class);
        assertNull(propertySource);

        propertySource = getPropertySource(environment, "test", MockPropertySource.class, MockPropertySource::new);
        assertNotNull(propertySource);
    }

    @Test
    public void testGetMapPropertySource() {
        PropertySource propertySource = getMapPropertySource(environment, MockPropertySource.MOCK_PROPERTIES_PROPERTY_SOURCE_NAME);
        assertNotNull(propertySource);

        propertySource = getMapPropertySource(environment, "test");
        assertNull(propertySource);

        propertySource = getMapPropertySource(environment, "test", true);
        assertNotNull(propertySource);

        propertySource = getMapPropertySource(environment, "test");
        assertNotNull(propertySource);
    }

    @Test
    public void testFindConfiguredPropertySource() {
        PropertySource propertySource = findConfiguredPropertySource(environment, "test-key");
        assertNotNull(propertySource);

        propertySource = findConfiguredPropertySource(environment, "non-exist-key");
        assertNull(propertySource);
    }

    @Test
    public void testFindConfiguredPropertySourceName() {
        String propertySourceName = findConfiguredPropertySourceName(environment, "test-key");
        assertNotNull(propertySourceName, MockPropertySource.MOCK_PROPERTIES_PROPERTY_SOURCE_NAME);

        propertySourceName = findConfiguredPropertySourceName(environment, "non-exist-key");
        assertNull(propertySourceName);
    }

    @Test
    public void testResolveCommaDelimitedValueToList() {
        List<String> values = resolveCommaDelimitedValueToList(environment, "${test-key},${test-key2}");
        assertEquals("test-value", values.get(0));
        assertEquals("test-value2", values.get(1));
    }

    @Test
    public void testGetConversionService() {
        ConversionService conversionService = getConversionService(environment);
        assertEquals(getSharedInstance(), conversionService);
    }

    @Test
    public void testFindPropertyNamesByPrefix() {
        Set<String> propertyNames = findPropertyNamesByPrefix(environment, "test-");
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains("test-key"));
        assertTrue(propertyNames.contains("test-key2"));
    }

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
