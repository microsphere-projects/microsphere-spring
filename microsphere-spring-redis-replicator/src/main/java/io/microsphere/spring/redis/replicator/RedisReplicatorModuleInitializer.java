package io.microsphere.spring.redis.replicator;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Redis Replicator Module Initializer
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface RedisReplicatorModuleInitializer {

    /**
     * Check whether the current module supports it
     *
     * @param applicationContext {@link ConfigurableApplicationContext}
     * @return Return <code>null<code> if supported, otherwise, return <code>false<code>
     */
    boolean supports(ConfigurableApplicationContext applicationContext);

    /**
     * The module initializes the Producer module function
     *
     * @param applicationContext {@link ConfigurableApplicationContext}
     * @param registry           {@link BeanDefinitionRegistry}
     */
    void initializeProducerModule(ConfigurableApplicationContext applicationContext, BeanDefinitionRegistry registry);

    /**
     * The module initializes the Consumer module function
     *
     * @param applicationContext {@link ConfigurableApplicationContext}
     * @param registry           {@link BeanDefinitionRegistry}
     */
    void initializeConsumerModule(ConfigurableApplicationContext applicationContext, BeanDefinitionRegistry registry);

}
