package io.microsphere.spring.redis.replicator.kafka.producer;

import io.microsphere.spring.redis.replicator.RedisReplicatorInitializer;
import io.microsphere.spring.redis.replicator.kafka.KafkaRedisReplicatorConfiguration;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.microsphere.spring.util.PropertySourcesUtils.getSubProperties;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;

/**
 * Redis Replicator Kafka producer configuration (loaded by {@link RedisReplicatorInitializer})
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see KafkaRedisReplicatorConfiguration
 * @see RedisReplicatorInitializer
 * @since 1.0.0
 */
public class KafkaProducerRedisReplicatorConfiguration extends KafkaRedisReplicatorConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerRedisReplicatorConfiguration.class);

    public static final String KAFKA_PRODUCER_PROPERTY_NAME_PREFIX = KAFKA_PROPERTY_NAME_PREFIX + "producer.";

    public static final String KAFKA_PRODUCER_KEY_PREFIX_PROPERTY_NAME = KAFKA_PROPERTY_NAME_PREFIX + "key-prefix";
    public static final String DEFAULT_KAFKA_PRODUCER_KEY_PREFIX = "RPE-";


    /**
     * Key Prefix
     */
    private String keyPrefix;

    private Map<String, Object> producerConfigs;

    private KafkaTemplate<byte[], byte[]> redisReplicatorKafkaTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        initKeyPrefix();
        initProducerConfigs();
        initRedisReplicatorKafkaTemplate();
    }

    private void initKeyPrefix() {
        this.keyPrefix = environment.getProperty(KAFKA_PRODUCER_KEY_PREFIX_PROPERTY_NAME, DEFAULT_KAFKA_PRODUCER_KEY_PREFIX);
    }

    private void initProducerConfigs() {
        Map<String, Object> producerConfigs = new HashMap<>();
        producerConfigs.put(BOOTSTRAP_SERVERS_CONFIG, brokerList);
        // Kafka Common properties
        producerConfigs.putAll(getSubProperties(environment, KAFKA_PROPERTY_NAME_PREFIX));
        // Kafka Producer properties
        producerConfigs.putAll(getSubProperties(environment, KAFKA_PRODUCER_PROPERTY_NAME_PREFIX));
        this.producerConfigs = producerConfigs;
    }

    private void initRedisReplicatorKafkaTemplate() {
        redisReplicatorKafkaTemplate = new KafkaTemplate<>(redisReplicatorProducerFactory());
    }

    /**
     * @return Redis Replicator {@link KafkaTemplate} (For internal use)
     */
    protected KafkaTemplate<byte[], byte[]> getRedisReplicatorKafkaTemplate() {
        return redisReplicatorKafkaTemplate;
    }

    protected String getKeyPrefix() {
        return keyPrefix;
    }

    private void destroyProducerFactory() {
        if (redisReplicatorKafkaTemplate != null) {
            ProducerFactory producerFactory = redisReplicatorKafkaTemplate.getProducerFactory();
            if (producerFactory instanceof DefaultKafkaProducerFactory) {
                DefaultKafkaProducerFactory defaultKafkaProducerFactory = (DefaultKafkaProducerFactory) producerFactory;
                defaultKafkaProducerFactory.reset();
            }
        }
    }

    private ProducerFactory<byte[], byte[]> redisReplicatorProducerFactory() {
        DefaultKafkaProducerFactory producerFactory = new DefaultKafkaProducerFactory<>(getRedisReplicatorProducerConfigs());
        producerFactory.setKeySerializer(new ByteArraySerializer());
        producerFactory.setValueSerializer(new ByteArraySerializer());
        return producerFactory;
    }

    private Map<String, Object> getRedisReplicatorProducerConfigs() {
        return producerConfigs;
    }

    @Override
    public void destroy() throws Exception {
        super.destroy();
        destroyProducerFactory();
    }
}
