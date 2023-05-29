package io.microsphere.spring.redis.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;

import static io.microsphere.spring.redis.config.RedisConfiguration.isEnabled;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactories;

/**
 * Redis Initializer
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RedisInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(RedisInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        if (supports(context)) {
            ClassLoader classLoader = context.getClassLoader();
            // Load RedisModuleInitializer list
            List<RedisModuleInitializer> redisModuleInitializers = loadFactories(RedisModuleInitializer.class, classLoader);
            // Sort RedisModuleInitializer list
            AnnotationAwareOrderComparator.sort(redisModuleInitializers);
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
            for (RedisModuleInitializer redisModuleInitializer : redisModuleInitializers) {
                boolean supports = redisModuleInitializer.supports(context, registry);
                logger.debug("ApplicationContext[id : '{}'] {} support to initialize RedisModuleInitializer[class : {} , order : {}]",
                        context.getId(), supports ? "does" : "does not", redisModuleInitializer.getClass(), redisModuleInitializer.getOrder());
                if (supports) {
                    redisModuleInitializer.initialize(context, registry);
                }
            }
        }
    }

    private boolean supports(ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (!(beanFactory instanceof BeanDefinitionRegistry)) {
            logger.warn("The application context [id: {}, class: {}]'s BeanFactory[class : {}] is not a {} type", context.getId(), context.getClass(), beanFactory.getClass(), AnnotationConfigRegistry.class);
            return false;
        }
        if (!isEnabled(context)) {
            return false;
        }
        return true;
    }
}
