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
    void testBeforeGetActiveProfiles() {
        this.environmentListener.beforeGetActiveProfiles(environment);
    }

    @Test
    void testAfterGetActiveProfiles() {
        this.environmentListener.afterGetActiveProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testBeforeGetDefaultProfiles() {
        this.environmentListener.beforeGetDefaultProfiles(environment);
    }

    @Test
    void testAfterGetDefaultProfiles() {
        this.environmentListener.afterGetDefaultProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testBeforeSetActiveProfiles() {
        this.environmentListener.beforeSetActiveProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testAfterSetActiveProfiles() {
        this.environmentListener.afterSetActiveProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testBeforeAddActiveProfile() {
        this.environmentListener.beforeAddActiveProfile(environment, "");
    }

    @Test
    void testAfterAddActiveProfile() {
        this.environmentListener.afterAddActiveProfile(environment, "");
    }

    @Test
    void testBeforeSetDefaultProfiles() {
        this.environmentListener.beforeSetDefaultProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testAfterSetDefaultProfiles() {
        this.environmentListener.afterSetDefaultProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testBeforeGetProperty() {
        this.environmentListener.beforeGetProperty(environment, "", String.class, null);
    }

    @Test
    void testAfterGetProperty() {
        this.environmentListener.afterGetProperty(environment, "", String.class, null, null);
    }

    @Test
    void testBeforeGetRequiredProperty() {
        this.environmentListener.beforeGetRequiredProperty(environment, "", String.class);
    }

    @Test
    void testAfterGetRequiredProperty() {
        this.environmentListener.afterGetRequiredProperty(environment, "", String.class, null);
    }

    @Test
    void testBeforeResolvePlaceholders() {
        this.environmentListener.beforeResolvePlaceholders(environment, "${}");
    }

    @Test
    void testAfterResolvePlaceholders() {
        this.environmentListener.afterResolvePlaceholders(environment, "${}", "");
    }

    @Test
    void testBeforeResolveRequiredPlaceholders() {
        this.environmentListener.beforeResolveRequiredPlaceholders(environment, "${}");
    }

    @Test
    void testAfterResolveRequiredPlaceholders() {
        this.environmentListener.afterResolveRequiredPlaceholders(environment, "${}", "");
    }

    @Test
    void testBeforeSetRequiredProperties() {
        this.environmentListener.beforeSetRequiredProperties(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testAfterSetRequiredProperties() {
        this.environmentListener.afterSetRequiredProperties(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testBeforeValidateRequiredProperties() {
        this.environmentListener.beforeValidateRequiredProperties(environment);
    }

    @Test
    void testAfterValidateRequiredProperties() {
        this.environmentListener.afterValidateRequiredProperties(environment);
    }

    @Test
    void testBeforeGetConversionService() {
        this.environmentListener.beforeGetConversionService(environment);
    }

    @Test
    void testAfterGetConversionService() {
        this.environmentListener.afterGetConversionService(environment, environment.getConversionService());
    }

    @Test
    void testBeforeSetConversionService() {
        this.environmentListener.beforeSetConversionService(environment, environment.getConversionService());
    }

    @Test
    void testAfterSetConversionService() {
        this.environmentListener.afterSetConversionService(environment, environment.getConversionService());
    }

    @Test
    void testBeforeSetPlaceholderPrefix() {
        this.environmentListener.beforeSetPlaceholderPrefix(environment, "");
    }

    @Test
    void testAfterSetPlaceholderPrefix() {
        this.environmentListener.afterSetPlaceholderPrefix(environment, "");
    }

    @Test
    void testBeforeSetPlaceholderSuffix() {
        this.environmentListener.beforeSetPlaceholderSuffix(environment, "");
    }

    @Test
    void testAfterSetPlaceholderSuffix() {
        this.environmentListener.afterSetPlaceholderSuffix(environment, "");
    }

    @Test
    void testBeforeSetIgnoreUnresolvableNestedPlaceholders() {
        this.environmentListener.beforeSetIgnoreUnresolvableNestedPlaceholders(environment, true);
    }

    @Test
    void testAfterSetIgnoreUnresolvableNestedPlaceholders() {
        this.environmentListener.afterSetIgnoreUnresolvableNestedPlaceholders(environment, true);
    }

    @Test
    void testBeforeSetValueSeparator() {
        this.environmentListener.beforeSetValueSeparator(environment, "");
    }

    @Test
    void testAfterSetValueSeparator() {
        this.environmentListener.afterSetValueSeparator(environment, "");
    }

    @Test
    void testBeforeGetPropertySources() {
        this.environmentListener.beforeGetPropertySources(environment);
    }

    @Test
    void testAfterGetPropertySources() {
        this.environmentListener.afterGetPropertySources(environment, environment.getPropertySources());
    }

    @Test
    void testBeforeGetSystemProperties() {
        this.environmentListener.beforeGetSystemProperties(environment);
    }

    @Test
    void testAfterGetSystemProperties() {
        this.environmentListener.afterGetSystemProperties(environment, environment.getSystemProperties());
    }

    @Test
    void testBeforeGetSystemEnvironment() {
        this.environmentListener.beforeGetSystemEnvironment(environment);
    }

    @Test
    void testAfterGetSystemEnvironment() {
        this.environmentListener.afterGetSystemEnvironment(environment, environment.getSystemEnvironment());
    }

    @Test
    void testBeforeMerge() {
        this.environmentListener.beforeMerge(environment, environment);
    }

    @Test
    void testAfterMerge() {
        this.environmentListener.afterMerge(environment, environment);
    }
}