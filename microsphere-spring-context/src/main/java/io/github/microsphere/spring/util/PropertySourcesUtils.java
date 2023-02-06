package io.github.microsphere.spring.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static io.github.microsphere.spring.util.ObjectUtils.EMPTY_STRING_ARRAY;
import static java.util.Collections.unmodifiableMap;

/**
 * {@link PropertySources} Utilities
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySources
 * @since 2017.01.19
 */
public abstract class PropertySourcesUtils {

    private static final Logger logger = LoggerFactory.getLogger(PropertySourcesUtils.class);

    public static final String DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME = "defaultProperties";

    /**
     * Get Sub {@link Properties}
     *
     * @param propertySources {@link PropertySource} Iterable
     * @param prefix          the prefix of property name
     * @return Map
     * @see Properties
     */
    public static Map<String, Object> getSubProperties(Iterable<PropertySource<?>> propertySources, String prefix) {

        MutablePropertySources mutablePropertySources = new MutablePropertySources();

        for (PropertySource<?> source : propertySources) {
            mutablePropertySources.addLast(source);
        }

        return getSubProperties(mutablePropertySources, prefix);

    }

    /**
     * Get Sub {@link Properties}
     *
     * @param environment {@link ConfigurableEnvironment}
     * @param prefix      the prefix of property name
     * @return Map
     * @see Properties
     */
    public static Map<String, Object> getSubProperties(ConfigurableEnvironment environment, String prefix) {
        return getSubProperties(environment.getPropertySources(), environment, prefix);
    }

    /**
     * Normalize the prefix
     *
     * @param prefix the prefix
     * @return the prefix
     */
    public static String normalizePrefix(String prefix) {
        return prefix.endsWith(".") ? prefix : prefix + ".";
    }

    /**
     * Get prefixed {@link Properties}
     *
     * @param propertySources {@link PropertySources}
     * @param prefix          the prefix of property name
     * @return Map
     * @see Properties
     * @since 1.0.3
     */
    public static Map<String, Object> getSubProperties(PropertySources propertySources, String prefix) {
        return getSubProperties(propertySources, new PropertySourcesPropertyResolver(propertySources), prefix);
    }

    /**
     * Get prefixed {@link Properties}
     *
     * @param propertySources  {@link PropertySources}
     * @param propertyResolver {@link PropertyResolver} to resolve the placeholder if present
     * @param prefix           the prefix of property name
     * @return Map
     * @see Properties
     * @since 1.0.3
     */
    public static Map<String, Object> getSubProperties(PropertySources propertySources, PropertyResolver propertyResolver, String prefix) {

        Map<String, Object> subProperties = new LinkedHashMap<String, Object>();

        String normalizedPrefix = normalizePrefix(prefix);

        Iterator<PropertySource<?>> iterator = propertySources.iterator();

        while (iterator.hasNext()) {
            PropertySource<?> source = iterator.next();
            for (String name : getPropertyNames(source)) {
                if (!subProperties.containsKey(name) && name.startsWith(normalizedPrefix)) {
                    String subName = name.substring(normalizedPrefix.length());
                    if (!subProperties.containsKey(subName)) { // take first one
                        Object value = source.getProperty(name);
                        if (value instanceof String) {
                            // Resolve placeholder
                            value = propertyResolver.resolvePlaceholders((String) value);
                        }
                        subProperties.put(subName, value);
                    }
                }
            }
        }

        return unmodifiableMap(subProperties);
    }

    /**
     * Get the property names as the array from the specified {@link PropertySource} instance.
     *
     * @param propertySource {@link PropertySource} instance
     * @return non-null
     * @since 1.0.10
     */
    public static String[] getPropertyNames(PropertySource propertySource) {
        String[] propertyNames = propertySource instanceof EnumerablePropertySource ? ((EnumerablePropertySource) propertySource).getPropertyNames() : null;

        if (propertyNames == null) {
            propertyNames = EMPTY_STRING_ARRAY;
        }

        return propertyNames;
    }

    public static void addDefaultProperties(ConfigurableEnvironment environment, String key, Object value, Object... others) {
        Map<String, Object> defaultProperties = getDefaultProperties(environment);
        defaultProperties.put(key, value);
        int length = others.length;
        for (int i = 0; i < length; ) {
            String k = String.valueOf(others[i++]);
            Object v = others[i++];
            defaultProperties.put(k, v);
        }
    }

    public static Map<String, Object> getDefaultProperties(ConfigurableEnvironment environment) {
        return getDefaultProperties(environment, true);
    }

    public static Map<String, Object> getDefaultProperties(ConfigurableEnvironment environment, boolean createIfAbsent) {
        Map<String, Object> defaultProperties = null;
        MapPropertySource defaultPropertiesPropertySource = getDefaultPropertiesPropertySource(environment, createIfAbsent);
        if (defaultPropertiesPropertySource != null) {
            defaultProperties = defaultPropertiesPropertySource.getSource();
            logger.debug("The 'defaultProperties' property was obtained successfully, and the current content is: {}", defaultProperties);
        }
        return defaultProperties;
    }

    public static MapPropertySource getDefaultPropertiesPropertySource(ConfigurableEnvironment environment) {
        return getDefaultPropertiesPropertySource(environment, true);
    }

    public static MapPropertySource getDefaultPropertiesPropertySource(ConfigurableEnvironment environment, boolean createIfAbsent) {
        MutablePropertySources propertySources = environment.getPropertySources();
        final String name = DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME;
        PropertySource propertySource = propertySources.get(name);
        MapPropertySource defaultPropertiesPropertySource = null;
        if (propertySource == null && createIfAbsent) {
            logger.warn("The 'defaultProperties' property will create an MapPropertySource[name:{}] by default", name);
            defaultPropertiesPropertySource = new MapPropertySource(name, new HashMap<>());
            propertySources.addLast(defaultPropertiesPropertySource);
        } else if (propertySource instanceof MapPropertySource) {
            logger.debug("The 'defaultProperties' property was initialized");
            defaultPropertiesPropertySource = (MapPropertySource) propertySource;
        } else {
            logger.warn("'defaultProperties' PropertySource[name: {}] is not an MapPropertySource instance; it is actually: {}", name, propertySource.getClass().getName());
        }
        return defaultPropertiesPropertySource;
    }
}

