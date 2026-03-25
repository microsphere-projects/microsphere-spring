package io.microsphere.spring.context.config;

import io.microsphere.logging.Logger;
import io.microsphere.logging.test.junit4.LoggingLevelsRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.logging.test.junit4.LoggingLevelsRule.levels;

/**
 * {@link ConfigurationBeanBinder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationBeanBinder
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class ConfigurationBeanBinderTest {

    @ClassRule
    public static final LoggingLevelsRule LOGGING_LEVELS_RULE = levels("TRACE", "INFO", "ERROR");


    private static final Logger logger = getLogger(ConfigurationBeanBinderTest.class);

    private ConfigurationBeanBinder configurationBeanBinder;

    private MockEnvironment mockEnvironment;

    @Before
    public void setUp() {
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