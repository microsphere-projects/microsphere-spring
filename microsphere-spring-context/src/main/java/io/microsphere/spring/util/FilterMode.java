package io.microsphere.spring.util;


import io.microsphere.annotation.ConfigurationProperty;
import org.springframework.core.env.Environment;

import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;

/**
 * The mode of Filter behaviors.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public enum FilterMode {

    /**
     * Sequential
     */
    SEQUENTIAL,

    /**
     * Conditional
     */
    CONDITIONAL;

    @ConfigurationProperty(
            defaultValue = "SEQUENTIAL",
            type = FilterMode.class,
            description = "The property name of the mode of Filter"
    )
    public static final String PROPERTY_NAME = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "filter-mode";

    /**
     * Parse {@link FilterMode} from {@link Environment}
     *
     * @param environment {@link Environment}
     * @return if {@link #PROPERTY_NAME property name} is present in the {@link Environment}, its' value
     * will be parsed as an instance {@link FilterMode}, or return {@link #SEQUENTIAL}
     */
    public static FilterMode valueOf(Environment environment) {
        return environment.getProperty(PROPERTY_NAME, FilterMode.class, SEQUENTIAL);
    }
}
