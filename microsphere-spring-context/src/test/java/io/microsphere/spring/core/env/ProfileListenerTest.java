package io.microsphere.spring.core.env;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;

/**
 * {@link ProfileListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ProfileListener
 * @since 1.0.0
 */
class ProfileListenerTest {

    private ProfileListener profileListener;

    private ConfigurableEnvironment environment;

    @BeforeEach
    void setUp() {
        this.profileListener = new ProfileListener() {
        };
        this.environment = new MockEnvironment();
    }

    @Test
    void testBeforeGetActiveProfiles() {
        this.profileListener.beforeGetActiveProfiles(environment);
    }

    @Test
    void testAfterGetActiveProfiles() {
        this.profileListener.afterGetActiveProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testBeforeGetDefaultProfiles() {
        this.profileListener.beforeGetDefaultProfiles(environment);
    }

    @Test
    void testAfterGetDefaultProfiles() {
        this.profileListener.afterGetDefaultProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testBeforeSetActiveProfiles() {
        this.profileListener.beforeSetActiveProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testAfterSetActiveProfiles() {
        this.profileListener.afterSetActiveProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testBeforeAddActiveProfile() {
        this.profileListener.beforeAddActiveProfile(environment, "");
    }

    @Test
    void testAfterAddActiveProfile() {
        this.profileListener.afterAddActiveProfile(environment, "");
    }

    @Test
    void testBeforeSetDefaultProfiles() {
        this.profileListener.beforeSetDefaultProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    void testAfterSetDefaultProfiles() {
        this.profileListener.afterSetDefaultProfiles(environment, EMPTY_STRING_ARRAY);
    }
}