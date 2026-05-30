package io.microsphere.spring.core.env;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;

/**
 * {@link PropertyResolverListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertyResolverListener
 * @since 1.0.0
 */
class PropertyResolverListenerTest {

    private PropertyResolverListener propertyResolverListener;

    private ConfigurableEnvironment environment;

    @BeforeEach
    void setUp() {
        this.propertyResolverListener = new PropertyResolverListener() {
        };
        this.environment = new MockEnvironment();
    }

    @Test
    void testBeforeGetProperty() {
        this.propertyResolverListener.beforeGetProperty(environment, "", String.class, null);
    }

    @Test
    void testAfterGetProperty() {
        this.propertyResolverListener.afterGetProperty(environment, "", String.class, null, null);
    }

    @Test
    void testBeforeGetRequiredProperty() {
        this.propertyResolverListener.beforeGetRequiredProperty(environment, "", String.class);
    }

    @Test
    void testAfterGetRequiredProperty() {
        this.propertyResolverListener.afterGetRequiredProperty(environment, "", String.class, null);
    }

    @Test
    void testBeforeResolvePlaceholders() {
        this.propertyResolverListener.beforeResolvePlaceholders(environment, "${}");
    }

    @Test
    void testAfterResolvePlaceholders() {
        this.propertyResolverListener.afterResolvePlaceholders(environment, "${}", "");
    }

    @Test
    void testBeforeResolveRequiredPlaceholders() {
        this.propertyResolverListener.beforeResolveRequiredPlaceholders(environment, "${}");
    }

    @Test
    void testAfterResolveRequiredPlaceholders() {
        this.propertyResolverListener.afterResolveRequiredPlaceholders(environment, "${}", "");
    }

    @Test
    void testBeforeSetRequiredProperties() {
        this.propertyResolverListener.beforeSetRequiredProperties(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testAfterSetRequiredProperties() {
        this.propertyResolverListener.afterSetRequiredProperties(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testBeforeValidateRequiredProperties() {
        this.propertyResolverListener.beforeValidateRequiredProperties(environment);
    }

    @Test
    void testAfterValidateRequiredProperties() {
        this.propertyResolverListener.afterValidateRequiredProperties(environment);
    }

    @Test
    void testBeforeGetConversionService() {
        this.propertyResolverListener.beforeGetConversionService(environment);
    }

    @Test
    void testAfterGetConversionService() {
        this.propertyResolverListener.afterGetConversionService(environment, environment.getConversionService());
    }

    @Test
    void testBeforeSetConversionService() {
        this.propertyResolverListener.beforeSetConversionService(environment, environment.getConversionService());
    }

    @Test
    void testAfterSetConversionService() {
        this.propertyResolverListener.afterSetConversionService(environment, environment.getConversionService());
    }

    @Test
    void testBeforeSetPlaceholderPrefix() {
        this.propertyResolverListener.beforeSetPlaceholderPrefix(environment, "");
    }

    @Test
    void testAfterSetPlaceholderPrefix() {
        this.propertyResolverListener.afterSetPlaceholderPrefix(environment, "");
    }

    @Test
    void testBeforeSetPlaceholderSuffix() {
        this.propertyResolverListener.beforeSetPlaceholderSuffix(environment, "");
    }

    @Test
    void testAfterSetPlaceholderSuffix() {
        this.propertyResolverListener.afterSetPlaceholderSuffix(environment, "");
    }

    @Test
    void testBeforeSetIgnoreUnresolvableNestedPlaceholders() {
        this.propertyResolverListener.beforeSetIgnoreUnresolvableNestedPlaceholders(environment, true);
    }

    @Test
    void testAfterSetIgnoreUnresolvableNestedPlaceholders() {
        this.propertyResolverListener.afterSetIgnoreUnresolvableNestedPlaceholders(environment, true);
    }

    @Test
    void testBeforeSetValueSeparator() {
        this.propertyResolverListener.beforeSetValueSeparator(environment, "");
    }

    @Test
    void testAfterSetValueSeparator() {
        this.propertyResolverListener.afterSetValueSeparator(environment, "");
    }
}