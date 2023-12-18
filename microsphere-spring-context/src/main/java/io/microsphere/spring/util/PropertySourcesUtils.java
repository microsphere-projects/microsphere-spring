package io.microsphere.spring.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.microsphere.collection.MapUtils.MIN_LOAD_FACTOR;
import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.spring.util.ObjectUtils.EMPTY_STRING_ARRAY;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * {@link PropertySources} Utilities
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySources
 * @since 1.0.0
 */
public abstract class PropertySourcesUtils {

    private static final Logger logger = LoggerFactory.getLogger(PropertySourcesUtils.class);

    /**
     * The {@link PropertySource#getName() PropertySource name} of {@link org.springframework.boot.SpringApplication#setDefaultProperties Spring Boot default poperties}
     *
     * @see org.springframework.boot.DefaultPropertiesPropertySource#addOrMerge(Map, MutablePropertySources)
     * @see org.springframework.boot.DefaultPropertiesPropertySource#NAME
     */
    public static final String DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME = "defaultProperties";

    /**
     * The name of the {@link PropertySource} {@link ConfigurationPropertySources#attach(Environment) adapter} from Spring Boot
     *
     * @see org.springframework.boot.context.properties.source.ConfigurationPropertySources#ATTACHED_PROPERTY_SOURCE_NAME
     * @see org.springframework.boot.context.properties.source.ConfigurationPropertySources#attach(Environment)
     */
    private static final String ATTACHED_PROPERTY_SOURCE_NAME = "configurationProperties";

    /**
     * The {@link PropertySource#getName() PropertySource name} of Spring Cloud Bootstrap context
     *
     * @see org.springframework.cloud.bootstrap.BootstrapApplicationListener#BOOTSTRAP_PROPERTY_SOURCE_NAME
     */
    public static final String BOOTSTRAP_PROPERTY_SOURCE_NAME = "bootstrap";


    public static <T extends PropertySource<?>> T getPropertySource(ConfigurableEnvironment environment, String propertySourceName,
                                                                    Class<T> propertySourceType) {
        return getPropertySource(environment, propertySourceName, propertySourceType, null);
    }

    public static <T extends PropertySource<?>> T getPropertySource(ConfigurableEnvironment environment, String propertySourceName,
                                                                    Class<T> propertySourceType, Supplier<T> propertySourceSupplierIfAbsent) {
        MutablePropertySources propertySources = environment.getPropertySources();
        PropertySource propertySource = propertySources.get(propertySourceName);
        T targetPropertySource = null;
        if (propertySource == null) {
            logger.debug("The '{}' PropertySource can't be found!", propertySourceName);
            if (propertySourceSupplierIfAbsent != null) {
                targetPropertySource = propertySourceSupplierIfAbsent.get();
                if (targetPropertySource != null) {
                    logger.debug("A new PropertySource[{}] will be created.", targetPropertySource);
                    propertySources.addLast(targetPropertySource);
                }
            }
        } else if (propertySourceType.isInstance(propertySource)) {
            logger.debug("The '{}' PropertySource[type: {}] was found!", propertySourceName, propertySource.getClass().getName());
            targetPropertySource = propertySourceType.cast(propertySource);
        } else {
            logger.warn("The '{}' PropertySource is not a {} instance, actual type : {}", propertySource.getClass().getName(),
                    propertySourceType.getName());
        }
        return targetPropertySource;
    }

    public static MapPropertySource getMapPropertySource(ConfigurableEnvironment environment, String propertySourceName) {
        return getMapPropertySource(environment, propertySourceName, false);
    }

    public static MapPropertySource getMapPropertySource(ConfigurableEnvironment environment, String propertySourceName, boolean created) {
        Supplier<MapPropertySource> propertySourceSupplierIfAbsent =
                created ? () -> new MapPropertySource(propertySourceName, new HashMap<>()) : null;
        return getPropertySource(environment, propertySourceName, MapPropertySource.class, propertySourceSupplierIfAbsent);
    }

    public static PropertySource findConfiguredPropertySource(ConfigurableEnvironment environment, String propertyName) {
        return findConfiguredPropertySource(environment.getPropertySources(), propertyName);
    }

    public static PropertySource findConfiguredPropertySource(Iterable<PropertySource<?>> propertySources, String propertyName) {
        PropertySource configuredPropertySource = null;
        for (PropertySource propertySource : propertySources) {
            if (propertySource.getName().equals(ATTACHED_PROPERTY_SOURCE_NAME)) {
                continue;
            }
            if (propertySource.containsProperty(propertyName)) {
                configuredPropertySource = propertySource;
                break;
            }
        }
        return configuredPropertySource;
    }

    public static String findConfiguredPropertySourceName(ConfigurableEnvironment environment, String propertyName) {
        return findConfiguredPropertySourceName(environment.getPropertySources(), propertyName);
    }

    public static String findConfiguredPropertySourceName(Iterable<PropertySource<?>> propertySources, String propertyName) {
        PropertySource configuredPropertySource = findConfiguredPropertySource(propertySources, propertyName);
        return configuredPropertySource == null ? null : configuredPropertySource.getName();
    }


    public static List<String> resolveCommaDelimitedValueToList(Environment environment, String commaDelimitedValue) {
        List<String> values = resolvePlaceholders(environment, commaDelimitedValue, List.class);
        return values == null ? emptyList() : unmodifiableList(values);
    }

    public static <T> T resolvePlaceholders(Environment environment, String propertyValue, Class<T> targetType) {
        return resolvePlaceholders(environment, propertyValue, targetType, null);
    }

    public static <T> T resolvePlaceholders(Environment environment, String propertyValue, Class<T> targetType, T defaultValue) {
        if (propertyValue == null) {
            return defaultValue;
        }
        ConversionService conversionService = getConversionService(environment);
        if (conversionService == null) {
            return defaultValue;
        }
        final T targetValue;
        String resolvedPropertyValue = environment.resolvePlaceholders(propertyValue);
        if (conversionService.canConvert(String.class, targetType)) {
            targetValue = conversionService.convert(resolvedPropertyValue, targetType);
            logger.debug("The property value[origin : {} , resolved : {}] was converted to be {}(type :{})!", propertyValue, resolvedPropertyValue,
                    targetValue, targetType);

        } else {
            targetValue = defaultValue;
            logger.debug(
                    "The property value[origin : {} , resolved : {}] can't be converted to be the target type[{}], take the default value({}) as result!",
                    propertyValue, resolvedPropertyValue, targetValue, targetType);
        }
        return targetValue;
    }

    public static ConversionService getConversionService(Environment environment) {
        ConversionService conversionService = null;
        if (environment instanceof ConfigurablePropertyResolver) {
            conversionService = ((ConfigurablePropertyResolver) environment).getConversionService();
            if (conversionService == null) {
                conversionService = DefaultConversionService.getSharedInstance();
                logger.warn("ConversionService can't be resolved from Environment[class: {}], the shared ApplicationConversionService will be used!",
                        environment.getClass().getName());
            }
        }
        return conversionService;
    }

    public static Set<String> findPropertyNamesByPrefix(ConfigurableEnvironment environment, String propertyNamePrefix) {
        return findPropertyNames(environment, propertyName -> propertyName.startsWith(propertyNamePrefix));
    }

    public static Set<String> findPropertyNames(ConfigurableEnvironment environment, Predicate<String> propertyNameFilter) {
        Set<String> propertyNames = new LinkedHashSet<>();
        for (PropertySource propertySource : environment.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource) {
                EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) propertySource;
                for (String propertyName : enumerablePropertySource.getPropertyNames()) {
                    if (propertyNameFilter.test(propertyName)) {
                        propertyNames.add(propertyName);
                    }
                }
            }
        }
        return unmodifiableSet(propertyNames);
    }

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
     * @since 1.0.0
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
     * @since 1.0.0
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
     * @since 1.0.0
     */
    @Nonnull
    public static String[] getPropertyNames(PropertySource propertySource) {
        String[] propertyNames = propertySource instanceof EnumerablePropertySource ? ((EnumerablePropertySource) propertySource).getPropertyNames() : null;

        if (propertyNames == null) {
            propertyNames = EMPTY_STRING_ARRAY;
        }

        return propertyNames;
    }

    /**
     * Get the {@link Map} as the properties from the specified {@link PropertySource}
     *
     * @param propertySource the specified {@link PropertySource}
     * @return non-null read-only {@link Map}
     */
    @Nonnull
    public static Map<String, Object> getProperties(PropertySource propertySource) {
        String[] propertyNames = getPropertyNames(propertySource);
        int length = propertyNames.length;
        if (length < 1) {
            return emptyMap();
        }
        Map<String, Object> properties = newLinkedHashMap(length, MIN_LOAD_FACTOR);
        for (int i = 0; i < length; i++) {
            String propertyName = propertyNames[i];
            properties.put(propertyName, propertySource.getProperty(propertyName));
        }
        return unmodifiableMap(properties);
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

    /**
     * Get the {@link Map} properties from the "default" {@link PropertySource} that is the lowest
     * order one of the Spring {@link PropertySources} is created if absent
     *
     * @param environment {@link ConfigurableEnvironment}
     * @return non-null mutable {@link Map}
     * @see #DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME
     * @see #getDefaultProperties(ConfigurableEnvironment, boolean)
     */
    public static Map<String, Object> getDefaultProperties(ConfigurableEnvironment environment) {
        return getDefaultProperties(environment, true);
    }

    /**
     * Get the {@link Map} properties from the "default" {@link PropertySource} that is the lowest
     * order one of the Spring {@link PropertySources} is created if specified.
     *
     * @param environment    {@link ConfigurableEnvironment}
     * @param createIfAbsent <code>true</code> indicates the "default" {@link PropertySource} will be created if absent
     * @return non-null mutable {@link Map}
     * @see #DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME
     * @see #getDefaultProperties(ConfigurableEnvironment, boolean)
     */
    public static Map<String, Object> getDefaultProperties(ConfigurableEnvironment environment, boolean createIfAbsent) {
        Map<String, Object> defaultProperties = null;
        MapPropertySource defaultPropertiesPropertySource = getDefaultPropertiesPropertySource(environment, createIfAbsent);
        if (defaultPropertiesPropertySource != null) {
            defaultProperties = defaultPropertiesPropertySource.getSource();
            logger.debug("The 'defaultProperties' property was obtained successfully, and the current content is: {}", defaultProperties);
        }
        return defaultProperties;
    }

    /**
     * Get the "default" {@link PropertySource} that is the lowest order one of the Spring {@link PropertySources}
     * is created if absent
     *
     * @param environment {@link ConfigurableEnvironment}
     * @return non-null {@link MapPropertySource}
     */
    public static MapPropertySource getDefaultPropertiesPropertySource(ConfigurableEnvironment environment) {
        return getDefaultPropertiesPropertySource(environment, true);
    }

    /**
     * Get the "default" {@link PropertySource} that is the lowest order one of the Spring {@link PropertySources}
     * is created if specified
     *
     * @param environment    {@link ConfigurableEnvironment}
     * @param createIfAbsent <code>true</code> indicates the "default" {@link PropertySource} will be created if absent
     * @return non-null {@link MapPropertySource}
     */
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

    /**
     * Contains the specified {@link PropertySource} or not
     *
     * @param environment        {@link ConfigurableEnvironment}
     * @param propertySourceName {@link PropertySource#getName() the PropertySource name}
     * @return if contains, return <code>true</code>, otherwise <code>false</code>
     */
    public static boolean containsPropertySource(ConfigurableEnvironment environment, String propertySourceName) {
        PropertySources propertySources = environment.getPropertySources();
        return propertySources.contains(propertySourceName);
    }

    /**
     * Contains the Bootstrap {@link PropertySource} or not
     *
     * @param environment {@link ConfigurableEnvironment}
     * @return if contains, return <code>true</code>, otherwise <code>false</code>
     */
    public static boolean containsBootstrapPropertySource(ConfigurableEnvironment environment) {
        return containsPropertySource(environment, BOOTSTRAP_PROPERTY_SOURCE_NAME);
    }
}

