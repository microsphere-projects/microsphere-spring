package io.microsphere.spring.context.config;

import io.microsphere.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * {@link ConfigurationBeanBinder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationBeanBinder
 * @since 1.0.0
 */
class ConfigurationBeanBinderTest {

    private static final Logger logger = getLogger(ConfigurationBeanBinderTest.class);

    private ConfigurationBeanBinder configurationBeanBinder;

    private MockEnvironment mockEnvironment;

    @BeforeEach
    void setUp() {
        this.configurationBeanBinder = (configurationProperties, ignoreUnknownFields, ignoreInvalidFields, configurationBean) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("configurationProperties : {} , ignoreUnknownFields : {} , ignoreInvalidFields : {} , configurationBean : {}",
                        configurationProperties, ignoreUnknownFields, ignoreInvalidFields, configurationBean);
            }
        };
        this.mockEnvironment = new MockEnvironment();
    }

    @Test
    public void testBind() {
        Map<String, Object> configurationProperties = this.mockEnvironment.getSystemProperties();
        this.configurationBeanBinder.bind(configurationProperties, false, false, new Object());
    }

    @Test
    public void testSetConversionService() {
        this.configurationBeanBinder.setConversionService(this.mockEnvironment.getConversionService());
    }
}