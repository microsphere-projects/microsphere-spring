package io.microsphere.spring.redis.config;

import io.microsphere.spring.redis.event.RedisConfigurationPropertyChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import static io.microsphere.spring.redis.util.RedisConstants.COMMAND_EVENT_EXPOSED_PROPERTY_NAME;
import static io.microsphere.spring.redis.util.RedisConstants.DEFAULT_COMMAND_EVENT_EXPOSED;
import static io.microsphere.spring.redis.util.RedisConstants.DEFAULT_ENABLED;
import static io.microsphere.spring.redis.util.RedisConstants.ENABLED_PROPERTY_NAME;

/**
 * Redis Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RedisConfiguration implements ApplicationListener<RedisConfigurationPropertyChangedEvent>, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfiguration.class);

    /**
     * RedisConfiguration
     * {@link RedisConfiguration} Bean Name
     */
    public static final String BEAN_NAME = "redisConfiguration";

    protected ConfigurableApplicationContext context;

    protected ConfigurableEnvironment environment;

    protected String applicationName;

    protected volatile boolean enabled;

    @Override
    public void onApplicationEvent(RedisConfigurationPropertyChangedEvent event) {
        if (event.hasProperty(ENABLED_PROPERTY_NAME)) {
            setEnabled();
        }
    }

    public void setEnabled() {
        this.enabled = isEnabled(context);
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected String resolveApplicationName(Environment environment) {
        String applicationName = environment.getProperty("spring.application.name", "default");
        return applicationName;
    }

    public ConfigurableEnvironment getEnvironment() {
        return environment;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public boolean isCommandEventExposed() {
        return isCommandEventExposed(context);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = (ConfigurableApplicationContext) context;
        this.environment = (ConfigurableEnvironment) context.getEnvironment();
        this.applicationName = resolveApplicationName(environment);
        setEnabled();
    }

    public static boolean isEnabled(ApplicationContext context) {
        return getBoolean(context, ENABLED_PROPERTY_NAME, DEFAULT_ENABLED, "Configuration", "enabled");
    }

    public static boolean isCommandEventExposed(ApplicationContext context) {
        return getBoolean(context, COMMAND_EVENT_EXPOSED_PROPERTY_NAME, DEFAULT_COMMAND_EVENT_EXPOSED, "Command Event", "exposed");
    }

    public static boolean getBoolean(ApplicationContext context, String propertyName, boolean defaultValue, String feature, String statusIfTrue) {
        Environment environment = context.getEnvironment();
        Boolean propertyValue = environment.getProperty(propertyName, Boolean.class);
        boolean value = propertyValue == null ? defaultValue : propertyValue.booleanValue();
        if (logger.isDebugEnabled()) {
            String status = value ? statusIfTrue : "not " + statusIfTrue;
            logger.debug("Microsphere Redis {} is '{}' in the Spring ApplicationContext[id :'{}' , property name: '{}' , property value: {} , default value: {}", feature, status, context.getId(), propertyName, propertyValue, defaultValue);
        }
        return value;
    }

    public static RedisConfiguration get(BeanFactory beanFactory) {
        return beanFactory.getBean(BEAN_NAME, RedisConfiguration.class);
    }
}
