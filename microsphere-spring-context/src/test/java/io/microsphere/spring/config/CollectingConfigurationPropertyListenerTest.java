package io.microsphere.spring.config;

import io.microsphere.beans.ConfigurationProperty;
import io.microsphere.spring.core.env.ListenableConfigurableEnvironment;
import io.microsphere.spring.core.env.ListenableConfigurableEnvironmentInitializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.util.SystemPropertyUtils.PLACEHOLDER_PREFIX;
import static org.springframework.util.SystemPropertyUtils.PLACEHOLDER_SUFFIX;

/**
 * {@link CollectingConfigurationPropertyListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see CollectingConfigurationPropertyListener
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(
        classes = {
                CollectingConfigurationPropertyListenerTest.class
        },
        initializers = {
                ListenableConfigurableEnvironmentInitializer.class
        }
)
@TestPropertySource(
        properties = {
                "test-name=test-value"
        }
)
public class CollectingConfigurationPropertyListenerTest {

    @Autowired
    private ListenableConfigurableEnvironment environment;

    @Autowired
    private ConfigurationPropertyRepository repository;

    @Autowired
    private CollectingConfigurationPropertyListener listener;

    @Test
    public void testAfterGetProperty() {
        String name = "test-name";
        assertEquals("test-value", environment.getProperty(name));
        ConfigurationProperty property = repository.get(name);
        assertProperty(property, false);
    }

    @Test
    public void testAfterGetRequiredProperty() {
        String name = "test-name";
        assertEquals("test-value", environment.getRequiredProperty(name));
        ConfigurationProperty property = repository.get(name);
        assertProperty(property, true);
    }

    @Test
    public void testAfterSetPlaceholderPrefix() {
        String placeholderPrefix = listener.getPlaceholderPrefix();
        assertEquals(PLACEHOLDER_PREFIX, placeholderPrefix);

        placeholderPrefix = "#(";
        environment.setPlaceholderPrefix(placeholderPrefix);
        assertEquals(placeholderPrefix, listener.getPlaceholderPrefix());
    }

    @Test
    public void testAfterSetPlaceholderSuffix() {
        String placeholderSuffix = listener.getPlaceholderSuffix();
        assertEquals(PLACEHOLDER_SUFFIX, placeholderSuffix);

        placeholderSuffix = ")";
        environment.setPlaceholderSuffix(placeholderSuffix);
        assertEquals(placeholderSuffix, listener.getPlaceholderSuffix());
    }

    private void assertProperty(ConfigurationProperty property, boolean required) {
        assertEquals("test-name", property.getName());
        assertEquals("test-value", property.getValue());
        assertEquals(required, property.isRequired());
        assertEquals(String.class.getName(), property.getType());
        assertNull(property.getDefaultValue());
    }
}