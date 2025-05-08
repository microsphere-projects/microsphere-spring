package io.microsphere.spring.core.env;

import org.junit.Before;
import org.junit.Test;
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
public class ProfileListenerTest {

    private ProfileListener profileListener;

    private ConfigurableEnvironment environment;

    @Before
    public void before() {
        this.profileListener = new ProfileListener() {
        };
        this.environment = new MockEnvironment();
    }

    @Test
    public void testBeforeGetActiveProfiles() {
        this.profileListener.beforeGetActiveProfiles(environment);
    }

    @Test
    public void testAfterGetActiveProfiles() {
        this.profileListener.afterGetActiveProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testBeforeGetDefaultProfiles() {
        this.profileListener.beforeGetDefaultProfiles(environment);
    }

    @Test
    public void testAfterGetDefaultProfiles() {
        this.profileListener.afterGetDefaultProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testBeforeSetActiveProfiles() {
        this.profileListener.beforeSetActiveProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testAfterSetActiveProfiles() {
        this.profileListener.afterSetActiveProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testBeforeAddActiveProfile() {
        this.profileListener.beforeAddActiveProfile(environment, "");
    }

    @Test
    public void testAfterAddActiveProfile() {
        this.profileListener.afterAddActiveProfile(environment, "");
    }

    @Test
    public void testBeforeSetDefaultProfiles() {
        this.profileListener.beforeSetDefaultProfiles(environment, EMPTY_STRING_ARRAY);
    }

    @Test
    public void testAfterSetDefaultProfiles() {
        this.profileListener.afterSetDefaultProfiles(environment, EMPTY_STRING_ARRAY);
    }
}