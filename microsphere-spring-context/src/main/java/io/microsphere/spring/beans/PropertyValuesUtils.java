package io.microsphere.spring.beans;

import io.microsphere.util.Utils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

import static io.microsphere.spring.core.env.PropertySourcesUtils.getSubProperties;

/**
 * {@link PropertyValues} Utilities
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertyValues
 * @since 1.0.0
 */
public abstract class PropertyValuesUtils implements Utils {

    /**
     * Extracts a subset of properties from the given {@link ConfigurableEnvironment} based on the specified prefix,
     * and returns them as a {@link PropertyValues} instance.
     *
     * <p>This method is useful when you need to isolate a group of related properties, such as those
     * belonging to a specific configuration section.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>
     * ConfigurableEnvironment environment = context.getEnvironment();
     * PropertyValues databaseProperties = PropertyValuesUtils.getSubPropertyValues(environment, "database.");
     * // This will include all properties under "database.*"
     * </pre>
     *
     * @param environment the {@link ConfigurableEnvironment} to extract properties from
     * @param prefix      the prefix used to filter properties (e.g., "database.")
     * @return a {@link PropertyValues} instance containing the filtered properties
     * @throws IllegalArgumentException if the provided environment is null
     */
    public static PropertyValues getSubPropertyValues(ConfigurableEnvironment environment, String prefix) {
        Map<String, Object> subProperties = getSubProperties(environment.getPropertySources(), environment, prefix);
        PropertyValues subPropertyValues = new MutablePropertyValues(subProperties);
        return subPropertyValues;
    }

    private PropertyValuesUtils() {
    }
}
