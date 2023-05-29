package io.microsphere.spring.redis.context;

import io.microsphere.spring.redis.annotation.EnableRedisInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.microsphere.spring.redis.config.RedisConfiguration.isCommandEventExposed;
import static io.microsphere.spring.redis.util.RedisConstants.DEFAULT_INTERCEPTOR_ENABLED;
import static io.microsphere.spring.redis.util.RedisConstants.DEFAULT_WRAP_REDIS_TEMPLATE_PLACEHOLDER;
import static io.microsphere.spring.redis.util.RedisConstants.INTERCEPTOR_ENABLED_PROPERTY_NAME;

/**
 * {@link RedisModuleInitializer RedisModuleInitializer} Interceptor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisModuleInitializer
 * @since 1.0.0
 */
public class RedisInterceptorModuleInitializer implements RedisModuleInitializer {

    private static final Logger logger = LoggerFactory.getLogger(RedisInterceptorModuleInitializer.class);

    @Override
    public boolean supports(ConfigurableApplicationContext context, BeanDefinitionRegistry registry) {
        ConfigurableEnvironment environment = context.getEnvironment();
        String propertyName = INTERCEPTOR_ENABLED_PROPERTY_NAME;
        boolean enabled = environment.getProperty(propertyName, boolean.class, DEFAULT_INTERCEPTOR_ENABLED);
        logger.debug("Microsphere Redis Interceptor is '{}'", enabled ? "Enabled" : "Disabled");
        return enabled;
    }

    @Override
    public void initialize(ConfigurableApplicationContext context, BeanDefinitionRegistry registry) {
        ConfigurableEnvironment environment = context.getEnvironment();
        AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(registry, environment);
        Class<?> configClass = isCommandEventExposed(context) ? Config.class : NoExposingCommandEventConfig.class;
        reader.register(configClass);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    @EnableRedisInterceptor(wrapRedisTemplates = DEFAULT_WRAP_REDIS_TEMPLATE_PLACEHOLDER)
    private static class Config {
    }

    @EnableRedisInterceptor(wrapRedisTemplates = DEFAULT_WRAP_REDIS_TEMPLATE_PLACEHOLDER, exposeCommandEvent = false)
    private static class NoExposingCommandEventConfig {
    }

}
