package io.github.microsphere.spring.redis.replicator;

import io.github.microsphere.spring.redis.context.RedisModuleInitializer;
import io.github.microsphere.spring.redis.replicator.config.RedisReplicatorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

import static io.github.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactories;

/**
 * Redis Replicator Initializer
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RedisReplicatorInitializer implements RedisModuleInitializer {

    private static final Logger logger = LoggerFactory.getLogger(RedisReplicatorInitializer.class);

    @Override
    public boolean supports(ConfigurableApplicationContext context, BeanDefinitionRegistry registry) {
        return RedisReplicatorConfiguration.isEnabled(context);
    }

    @Override
    public void initialize(ConfigurableApplicationContext context, BeanDefinitionRegistry registry) {

        RedisReplicatorConfiguration redisReplicatorConfiguration = createRedisReplicatorConfiguration(context);

        registerRedisReplicatorConfiguration(redisReplicatorConfiguration, context);

        boolean consumerEnabled = redisReplicatorConfiguration.isConsumerEnabled();

        if (consumerEnabled) {
            registerConsumerComponents(context, registry);
        }

        ClassLoader classLoader = context.getClassLoader();

        List<RedisReplicatorModuleInitializer> redisReplicatorModuleInitializers = loadFactories(RedisReplicatorModuleInitializer.class, classLoader);

        initializeRedisReplicatorModuleInitializers(redisReplicatorModuleInitializers, context, registry, consumerEnabled);

    }

    private void registerConsumerComponents(ConfigurableApplicationContext context, BeanDefinitionRegistry registry) {
        registerRedisCommandReplicator(registry);
    }

    private void registerRedisCommandReplicator(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, RedisCommandReplicator.BEAN_NAME, RedisCommandReplicator.class);
    }

    private RedisReplicatorConfiguration createRedisReplicatorConfiguration(ConfigurableApplicationContext context) {
        return new RedisReplicatorConfiguration(context);
    }

    private void registerRedisReplicatorConfiguration(RedisReplicatorConfiguration redisReplicatorConfiguration, ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        beanFactory.registerSingleton(RedisReplicatorConfiguration.BEAN_NAME, redisReplicatorConfiguration);
    }

    private void initializeRedisReplicatorModuleInitializers(List<RedisReplicatorModuleInitializer> redisReplicatorModuleInitializers,
                                                             ConfigurableApplicationContext context,
                                                             BeanDefinitionRegistry registry,
                                                             boolean isConsumerModule) {
        for (RedisReplicatorModuleInitializer redisReplicatorModuleInitializer : redisReplicatorModuleInitializers) {
            if (redisReplicatorModuleInitializer.supports(context)) {
                if (isConsumerModule) {
                    redisReplicatorModuleInitializer.initializeConsumerModule(context, registry);
                } else {
                    redisReplicatorModuleInitializer.initializeProducerModule(context, registry);
                }
                logger.debug("Application context [id: {}] Initializes Redis Replicator {} module [{}]!",
                        context.getId(),
                        isConsumerModule ? "Consumer" : "Producer",
                        redisReplicatorModuleInitializer.getClass().getName()
                );
            }
        }


    }

    @Override
    public int getOrder() {
        return 0;
    }
}