package io.microsphere.spring.core.env;

import org.junit.Before;
import org.junit.Test;
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
public class PropertyResolverListenerTest {

    private PropertyResolverListener propertyResolverListener;

    private ConfigurableEnvironment environment;

    @Before
    public void before() {
        this.propertyResolverListener = new PropertyResolverListener() {
        };
        this.environment = new MockEnvironment();
    }

    @Test
    public void testBeforeGetProperty() {
        this.propertyResolverListener.beforeGetProperty(environment, "", String.class, null);
    }

    @Test
    public void testAfterGetProperty() {
        this.propertyResolverListener.afterGetProperty(environment, "", String.class, null, null);
    }

    @Test
    public void testBeforeGetRequiredProperty() {
        this.propertyResolverListener.beforeGetRequiredProperty(environment, "", String.class);
    }

    @Test
    public void testAfterGetRequiredProperty() {
        this.propertyResolverListener.afterGetRequiredProperty(environment, "", String.class, null);
    }

    @Test
    public void testBeforeResolvePlaceholders() {
        this.propertyResolverListener.beforeResolvePlaceholders(environment, "${}");
    }

    @Test
    public void testAfterResolvePlaceholders() {
        this.propertyResolverListener.afterResolvePlaceholders(environment, "${}", "");
    }

    @Test
    public void testBeforeResolveRequiredPlaceholders() {
        this.propertyResolverListener.beforeResolveRequiredPlaceholders(environment, "${}");
    }

    @Test
    public void testAfterResolveRequiredPlaceholders() {
        this.propertyResolverListener.afterResolveRequiredPlaceholders(environment, "${}", "");
    }

    @Test
    public void testBeforeSetRequiredProperties() {
        this.propertyResolverListener.beforeSetRequiredProperties(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testAfterSetRequiredProperties() {
        this.propertyResolverListener.afterSetRequiredProperties(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testBeforeValidateRequiredProperties() {
        this.propertyResolverListener.beforeValidateRequiredProperties(environment);
    }

    @Test
    public void testAfterValidateRequiredProperties() {
        this.propertyResolverListener.afterValidateRequiredProperties(environment);
    }

    @Test
    public void testBeforeGetConversionService() {
        this.propertyResolverListener.beforeGetConversionService(environment);
    }

    @Test
    public void testAfterGetConversionService() {
        this.propertyResolverListener.afterGetConversionService(environment, environment.getConversionService());
    }

    @Test
    public void testBeforeSetConversionService() {
        this.propertyResolverListener.beforeSetConversionService(environment, environment.getConversionService());
    }

    @Test
    public void testAfterSetConversionService() {
        this.propertyResolverListener.afterSetConversionService(environment, environment.getConversionService());
    }

    @Test
    public void testBeforeSetPlaceholderPrefix() {
        this.propertyResolverListener.beforeSetPlaceholderPrefix(environment, "");
    }

    @Test
    public void testAfterSetPlaceholderPrefix() {
        this.propertyResolverListener.afterSetPlaceholderPrefix(environment, "");
    }

    @Test
    public void testBeforeSetPlaceholderSuffix() {
        this.propertyResolverListener.beforeSetPlaceholderSuffix(environment, "");
    }

    @Test
    public void testAfterSetPlaceholderSuffix() {
        this.propertyResolverListener.afterSetPlaceholderSuffix(environment, "");
    }

    @Test
    public void testBeforeSetIgnoreUnresolvableNestedPlaceholders() {
        this.propertyResolverListener.beforeSetIgnoreUnresolvableNestedPlaceholders(environment, true);
    }

    @Test
    public void testAfterSetIgnoreUnresolvableNestedPlaceholders() {
        this.propertyResolverListener.afterSetIgnoreUnresolvableNestedPlaceholders(environment, true);
    }

    @Test
    public void testBeforeSetValueSeparator() {
        this.propertyResolverListener.beforeSetValueSeparator(environment, "");
    }

    @Test
    public void testAfterSetValueSeparator() {
        this.propertyResolverListener.afterSetValueSeparator(environment, "");
    }
}