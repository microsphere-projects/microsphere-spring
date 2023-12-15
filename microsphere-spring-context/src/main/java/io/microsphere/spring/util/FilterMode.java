package io.microsphere.spring.util;


import org.springframework.core.env.Environment;

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

    public static final String PROPERTY_NAME = "microsphere.spring.filter-mode";

    /**
     * Parse {@link FilterMode} from {@link Environment}
     *
     * @param environment {@link Environment}
     * @return if {@link #PROPERTY_NAME property name} is present in the {@link Environment}, its' value
     *         will be parsed as an instance {@link FilterMode}, or return {@link #SEQUENTIAL}
     */
    public static FilterMode valueOf(Environment environment) {
        return environment.getProperty(PROPERTY_NAME, FilterMode.class, SEQUENTIAL);
    }
}
