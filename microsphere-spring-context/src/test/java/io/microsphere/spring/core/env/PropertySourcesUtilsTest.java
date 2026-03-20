package io.microsphere.spring.core.env;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.env.MockPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.spring.core.env.EnvironmentUtils.getConversionService;
import static io.microsphere.spring.core.env.EnvironmentUtils.resolveCommaDelimitedValueToList;
import static io.microsphere.spring.core.env.PropertySourcesUtils.DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME;
import static io.microsphere.spring.core.env.PropertySourcesUtils.addDefaultProperties;
import static io.microsphere.spring.core.env.PropertySourcesUtils.findConfiguredPropertySource;
import static io.microsphere.spring.core.env.PropertySourcesUtils.findConfiguredPropertySourceName;
import static io.microsphere.spring.core.env.PropertySourcesUtils.findPropertyNamesByPrefix;
import static io.microsphere.spring.core.env.PropertySourcesUtils.getDefaultProperties;
import static io.microsphere.spring.core.env.PropertySourcesUtils.getDefaultPropertiesPropertySource;
import static io.microsphere.spring.core.env.PropertySourcesUtils.getMapPropertySource;
import static io.microsphere.spring.core.env.PropertySourcesUtils.getPropertySource;
import static io.microsphere.spring.core.env.PropertySourcesUtils.getSubProperties;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.core.convert.support.DefaultConversionService.getSharedInstance;

/**
 * {@link PropertySourcesUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourcesUtils
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
class PropertySourcesUtilsTest {

    private ConfigurableEnvironment environment;

    @BeforeEach
    void setUp() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("test-key", "test-value");
        mockEnvironment.setProperty("test-key2", "test-value2");
        mockEnvironment.setConversionService((ConfigurableConversionService) getSharedInstance());
        this.environment = mockEnvironment;
    }

    @Test
    void testGetPropertySource() {
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
    void testGetMapPropertySource() {
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
    void testFindConfiguredPropertySource() {
        PropertySource propertySource = findConfiguredPropertySource(environment, "test-key");
        assertNotNull(propertySource);

        propertySource = findConfiguredPropertySource(environment, "non-exist-key");
        assertNull(propertySource);
    }

    @Test
    void testFindConfiguredPropertySourceName() {
        String propertySourceName = findConfiguredPropertySourceName(environment, "test-key");
        assertNotNull(propertySourceName, MockPropertySource.MOCK_PROPERTIES_PROPERTY_SOURCE_NAME);

        propertySourceName = findConfiguredPropertySourceName(environment, "non-exist-key");
        assertNull(propertySourceName);
    }

    @Test
    void testResolveCommaDelimitedValueToList() {
        List<String> values = resolveCommaDelimitedValueToList(environment, "${test-key},${test-key2}");
        assertEquals("test-value", values.get(0));
        assertEquals("test-value2", values.get(1));
    }

    @Test
    void testGetConversionService() {
        ConversionService conversionService = getConversionService(environment);
        assertEquals(getSharedInstance(), conversionService);
    }

    @Test
    void testFindPropertyNamesByPrefix() {
        Set<String> propertyNames = findPropertyNamesByPrefix(environment, "test-");
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains("test-key"));
        assertTrue(propertyNames.contains("test-key2"));
    }

    @Test
    void testGetSubProperties() {

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

        assertEquals(emptyMap(), result);

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

        assertEquals(emptyMap(), result);

        result = getSubProperties(propertySources, "no-exists");

        assertEquals(emptyMap(), result);

    }

    @Test
    void testDefaultProperties() {
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

    // ---- New tests to increase coverage ----

    /** getDefaultProperties(env) single-arg creates the source if absent. */
    @Test
    void testGetDefaultPropertiesSingleArgCreatesIfAbsent() {
        MockEnvironment env = new MockEnvironment();
        // No "defaultProperties" source yet → must be created
        Map<String, Object> props = getDefaultProperties(env);
        assertNotNull(props);
    }

    /** getDefaultPropertiesPropertySource(env) single-arg creates source if absent. */
    @Test
    void testGetDefaultPropertiesPropertySourceSingleArgCreatesIfAbsent() {
        MockEnvironment env = new MockEnvironment();
        MapPropertySource source = getDefaultPropertiesPropertySource(env);
        assertNotNull(source);
        assertEquals(DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME, source.getName());
    }

    /** getDefaultPropertiesPropertySource with createIfAbsent=false when missing returns null (via getDefaultProperties). */
    @Test
    void testGetDefaultPropertiesPropertySourceNotCreatedWhenAbsent() {
        MockEnvironment env = new MockEnvironment();
        // First call creates the source (createIfAbsent=true is the default)
        getDefaultPropertiesPropertySource(env);
        // Second call with createIfAbsent=false should find the already-existing source
        MapPropertySource source = getDefaultPropertiesPropertySource(env, false);
        assertNotNull(source);
        assertEquals(DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME, source.getName());
    }

    /** containsPropertySource returns true for an existing source, false otherwise. */
    @Test
    void testContainsPropertySource() {
        assertTrue(PropertySourcesUtils.containsPropertySource(
                environment, MockPropertySource.MOCK_PROPERTIES_PROPERTY_SOURCE_NAME));
        assertFalse(PropertySourcesUtils.containsPropertySource(environment, "non-existent-source"));
    }

    /** containsBootstrapPropertySource returns false when the bootstrap source is absent. */
    @Test
    void testContainsBootstrapPropertySource() {
        assertFalse(PropertySourcesUtils.containsBootstrapPropertySource(environment));
    }

    /** getProperties(enumerable) returns a map of all properties. */
    @Test
    void testGetPropertiesFromEnumerablePropertySource() {
        MockPropertySource mock = new MockPropertySource("ps1");
        mock.setProperty("p1", "v1");
        mock.setProperty("p2", "v2");

        Map<String, Object> props = PropertySourcesUtils.getProperties(mock);
        assertEquals("v1", props.get("p1"));
        assertEquals("v2", props.get("p2"));
    }

    /** getProperties(non-enumerable) returns an empty map. */
    @Test
    void testGetPropertiesFromNonEnumerablePropertySource() {
        // Use an anonymous PropertySource that is not EnumerablePropertySource
        PropertySource<?> nonEnum = new PropertySource<Object>("non-enum", new Object()) {
            @Override
            public Object getProperty(String name) {
                return null;
            }
        };
        Map<String, Object> props = PropertySourcesUtils.getProperties(nonEnum);
        assertTrue(props.isEmpty());
    }

    /** getPropertyNames(non-enumerable) returns EMPTY_STRING_ARRAY. */
    @Test
    void testGetPropertyNamesFromNonEnumerablePropertySource() {
        PropertySource<?> nonEnum = new PropertySource<Object>("non-enum2", new Object()) {
            @Override
            public Object getProperty(String name) {
                return null;
            }
        };
        String[] names = PropertySourcesUtils.getPropertyNames(nonEnum);
        assertEquals(0, names.length);
    }

    /** findPropertyNames with a custom predicate returns matching property names. */
    @Test
    void testFindPropertyNamesWithCustomPredicate() {
        Set<String> names = PropertySourcesUtils.findPropertyNames(environment, name -> name.endsWith("2"));
        assertTrue(names.contains("test-key2"));
        assertFalse(names.contains("test-key"));
    }

    /** findConfiguredPropertySource(propertySources, name) skips the "configurationProperties" source. */
    @Test
    void testFindConfiguredPropertySourceSkipsAttachedSource() {
        MutablePropertySources propertySources = new MutablePropertySources();
        // Add the "configurationProperties" attached source that should be skipped
        MockPropertySource attached = new MockPropertySource("configurationProperties");
        attached.setProperty("skip-key", "skip-value");
        propertySources.addFirst(attached);

        MockPropertySource real = new MockPropertySource("real");
        real.setProperty("real-key", "real-value");
        propertySources.addLast(real);

        // "skip-key" lives in "configurationProperties" which is skipped
        PropertySource found = PropertySourcesUtils.findConfiguredPropertySource(propertySources, "skip-key");
        assertNull(found);

        // "real-key" is in "real" which is not skipped
        found = PropertySourcesUtils.findConfiguredPropertySource(propertySources, "real-key");
        assertNotNull(found);
        assertEquals("real", found.getName());
    }

}
