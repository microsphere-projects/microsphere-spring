package io.microsphere.spring.core.env;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;

/**
 * {@link EnvironmentListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnvironmentListener
 * @since 1.0.0
 */
class EnvironmentListenerTest {

    private EnvironmentListener environmentListener;

    private ConfigurableEnvironment environment;

    @BeforeEach
    void setUp() {
        this.environmentListener = new EnvironmentListener() {
        };
        this.environment = new MockEnvironment();
    }

    @Test
    public void testBeforeGetActiveProfiles() {
        this.environmentListener.beforeGetActiveProfiles(environment);
    }

    @Test
    public void testAfterGetActiveProfiles() {
        this.environmentListener.afterGetActiveProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testBeforeGetDefaultProfiles() {
        this.environmentListener.beforeGetDefaultProfiles(environment);
    }

    @Test
    public void testAfterGetDefaultProfiles() {
        this.environmentListener.afterGetDefaultProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testBeforeSetActiveProfiles() {
        this.environmentListener.beforeSetActiveProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testAfterSetActiveProfiles() {
        this.environmentListener.afterSetActiveProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testBeforeAddActiveProfile() {
        this.environmentListener.beforeAddActiveProfile(environment, "");
    }

    @Test
    public void testAfterAddActiveProfile() {
        this.environmentListener.afterAddActiveProfile(environment, "");
    }

    @Test
    public void testBeforeSetDefaultProfiles() {
        this.environmentListener.beforeSetDefaultProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testAfterSetDefaultProfiles() {
        this.environmentListener.afterSetDefaultProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testBeforeGetProperty() {
        this.environmentListener.beforeGetProperty(environment, "", String.class, null);
    }

    @Test
    public void testAfterGetProperty() {
        this.environmentListener.afterGetProperty(environment, "", String.class, null, null);
    }

    @Test
    public void testBeforeGetRequiredProperty() {
        this.environmentListener.beforeGetRequiredProperty(environment, "", String.class);
    }

    @Test
    public void testAfterGetRequiredProperty() {
        this.environmentListener.afterGetRequiredProperty(environment, "", String.class, null);
    }

    @Test
    public void testBeforeResolvePlaceholders() {
        this.environmentListener.beforeResolvePlaceholders(environment, "${}");
    }

    @Test
    public void testAfterResolvePlaceholders() {
        this.environmentListener.afterResolvePlaceholders(environment, "${}", "");
    }

    @Test
    public void testBeforeResolveRequiredPlaceholders() {
        this.environmentListener.beforeResolveRequiredPlaceholders(environment, "${}");
    }

    @Test
    public void testAfterResolveRequiredPlaceholders() {
        this.environmentListener.afterResolveRequiredPlaceholders(environment, "${}", "");
    }

    @Test
    public void testBeforeSetRequiredProperties() {
        this.environmentListener.beforeSetRequiredProperties(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testAfterSetRequiredProperties() {
        this.environmentListener.afterSetRequiredProperties(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testBeforeValidateRequiredProperties() {
        this.environmentListener.beforeValidateRequiredProperties(environment);
    }

    @Test
    public void testAfterValidateRequiredProperties() {
        this.environmentListener.afterValidateRequiredProperties(environment);
    }

    @Test
    public void testBeforeGetConversionService() {
        this.environmentListener.beforeGetConversionService(environment);
    }

    @Test
    public void testAfterGetConversionService() {
        this.environmentListener.afterGetConversionService(environment, environment.getConversionService());
    }

    @Test
    public void testBeforeSetConversionService() {
        this.environmentListener.beforeSetConversionService(environment, environment.getConversionService());
    }

    @Test
    public void testAfterSetConversionService() {
        this.environmentListener.afterSetConversionService(environment, environment.getConversionService());
    }

    @Test
    public void testBeforeSetPlaceholderPrefix() {
        this.environmentListener.beforeSetPlaceholderPrefix(environment, "");
    }

    @Test
    public void testAfterSetPlaceholderPrefix() {
        this.environmentListener.afterSetPlaceholderPrefix(environment, "");
    }

    @Test
    public void testBeforeSetPlaceholderSuffix() {
        this.environmentListener.beforeSetPlaceholderSuffix(environment, "");
    }

    @Test
    public void testAfterSetPlaceholderSuffix() {
        this.environmentListener.afterSetPlaceholderSuffix(environment, "");
    }

    @Test
    public void testBeforeSetIgnoreUnresolvableNestedPlaceholders() {
        this.environmentListener.beforeSetIgnoreUnresolvableNestedPlaceholders(environment, true);
    }

    @Test
    public void testAfterSetIgnoreUnresolvableNestedPlaceholders() {
        this.environmentListener.afterSetIgnoreUnresolvableNestedPlaceholders(environment, true);
    }

    @Test
    public void testBeforeSetValueSeparator() {
        this.environmentListener.beforeSetValueSeparator(environment, "");
    }

    @Test
    public void testAfterSetValueSeparator() {
        this.environmentListener.afterSetValueSeparator(environment, "");
    }

    @Test
    public void testBeforeGetPropertySources() {
        this.environmentListener.beforeGetPropertySources(environment);
    }

    @Test
    public void testAfterGetPropertySources() {
        this.environmentListener.afterGetPropertySources(environment, environment.getPropertySources());
    }

    @Test
    public void testBeforeGetSystemProperties() {
        this.environmentListener.beforeGetSystemProperties(environment);
    }

    @Test
    public void testAfterGetSystemProperties() {
        this.environmentListener.afterGetSystemProperties(environment, environment.getSystemProperties());
    }

    @Test
    public void testBeforeGetSystemEnvironment() {
        this.environmentListener.beforeGetSystemEnvironment(environment);
    }

    @Test
    public void testAfterGetSystemEnvironment() {
        this.environmentListener.afterGetSystemEnvironment(environment, environment.getSystemEnvironment());
    }

    @Test
    public void testBeforeMerge() {
        this.environmentListener.beforeMerge(environment, environment);
    }

    @Test
    public void testAfterMerge() {
        this.environmentListener.afterMerge(environment, environment);
    }
}