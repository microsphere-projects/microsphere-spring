package io.microsphere.spring.redis.context;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

/**
 * Redis Module Initializer
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface RedisModuleInitializer extends Ordered {

    /**
     * Check whether the current module supports it
     *
     * @param context {@link ConfigurableApplicationContext}
     * @param registry           {@link BeanDefinitionRegistry}
     * @return Return <code>null<code> if supported, otherwise, return <code>false<code>
     */
    boolean supports(ConfigurableApplicationContext context, BeanDefinitionRegistry registry);

    /**
     * The module initializes the features
     *
     * @param context {@link ConfigurableApplicationContext}
     * @param registry           {@link BeanDefinitionRegistry}
     */
    void initialize(ConfigurableApplicationContext context, BeanDefinitionRegistry registry);
}
