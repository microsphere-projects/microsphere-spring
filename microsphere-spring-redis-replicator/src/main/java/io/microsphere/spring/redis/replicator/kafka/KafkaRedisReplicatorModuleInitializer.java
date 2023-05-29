package io.microsphere.spring.redis.replicator.kafka;

import io.microsphere.spring.redis.replicator.RedisReplicatorModuleInitializer;
import io.microsphere.spring.redis.replicator.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration;
import io.microsphere.spring.redis.replicator.kafka.producer.KafkaProducerRedisCommandEventListener;
import io.microsphere.spring.redis.replicator.kafka.producer.KafkaProducerRedisReplicatorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import static io.microsphere.spring.redis.replicator.kafka.KafkaRedisReplicatorConfiguration.KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME;
import static io.microsphere.spring.redis.replicator.kafka.KafkaRedisReplicatorConfiguration.SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME;
import static io.microsphere.spring.redis.replicator.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.KAFKA_CONSUMER_ENABLED_PROPERTY_NAME;
import static io.microsphere.spring.util.AnnotatedBeanDefinitionRegistryUtils.registerBeans;
import static io.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;

/**
 * Kafka {@link RedisReplicatorModuleInitializer}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class KafkaRedisReplicatorModuleInitializer implements RedisReplicatorModuleInitializer {

    private static final String KAFKA_TEMPLATE_CLASS_NAME = "org.springframework.kafka.core.KafkaTemplate";

    private static final Logger logger = LoggerFactory.getLogger(KafkaRedisReplicatorModuleInitializer.class);

    @Override
    public boolean supports(ConfigurableApplicationContext context) {
        if (!isClassPresent(context)) {
            logger.warn("spring-kafka and its related artifacts are not found in the class-path of application context [id: '{}'] . " + "The Kafka module will not be enabled!", context.getId());
            return false;
        }
        if (!hasBootstrapServers(context)) {
            logger.warn("Application context [id: '{}'] If the spring-kafka server cluster address is not configured, " + "The Kafka module will not be enabled!", context.getId());
            return false;
        }
        return true;
    }

    @Override
    public void initializeProducerModule(ConfigurableApplicationContext context, BeanDefinitionRegistry registry) {
        registerBeans(registry, KafkaProducerRedisReplicatorConfiguration.class);
        registerBeanDefinition(registry, KafkaProducerRedisCommandEventListener.class);
    }

    @Override
    public void initializeConsumerModule(ConfigurableApplicationContext context, BeanDefinitionRegistry registry) {
        if (!KafkaConsumerRedisReplicatorConfiguration.isEnabled(context)) {
            logger.warn("Application context [id: '{}'] Redis Replicator Kafka Consumer is not activated, you can configure Spring property {} = true to enable!", context.getId(), KAFKA_CONSUMER_ENABLED_PROPERTY_NAME);
            return;
        }
        registerBeans(registry, KafkaConsumerRedisReplicatorConfiguration.class);
    }


    private boolean isClassPresent(ConfigurableApplicationContext context) {
        ClassLoader classLoader = context.getClassLoader();
        return ClassUtils.isPresent(KAFKA_TEMPLATE_CLASS_NAME, classLoader);
    }

    private boolean hasBootstrapServers(ConfigurableApplicationContext context) {
        Environment environment = context.getEnvironment();
        return environment.containsProperty(KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME) || environment.containsProperty(SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME);
    }
}
