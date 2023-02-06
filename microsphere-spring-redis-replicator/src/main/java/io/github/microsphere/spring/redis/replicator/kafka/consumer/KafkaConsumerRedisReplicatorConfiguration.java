package io.github.microsphere.spring.redis.replicator.kafka.consumer;

import io.github.microsphere.spring.redis.config.RedisConfiguration;
import io.github.microsphere.spring.redis.event.RedisCommandEvent;
import io.github.microsphere.spring.redis.replicator.event.RedisCommandReplicatedEvent;
import io.github.microsphere.spring.redis.replicator.kafka.KafkaRedisReplicatorConfiguration;
import io.github.microsphere.spring.redis.serializer.Serializers;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.BatchAcknowledgingMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

import static io.github.microsphere.spring.redis.config.RedisConfiguration.getBoolean;
import static io.github.microsphere.spring.util.PropertySourcesUtils.getSubProperties;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.GROUP_ID_CONFIG;

/**
 * Kafka Consumer {@link KafkaRedisReplicatorConfiguration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class KafkaConsumerRedisReplicatorConfiguration extends KafkaRedisReplicatorConfiguration implements ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerRedisReplicatorConfiguration.class);

    public static final String KAFKA_CONSUMER_PROPERTY_NAME_PREFIX = KafkaRedisReplicatorConfiguration.KAFKA_PROPERTY_NAME_PREFIX + "consumer.";

    public static final String KAFKA_CONSUMER_ENABLED_PROPERTY_NAME = KAFKA_CONSUMER_PROPERTY_NAME_PREFIX + "enabled";

    public static final String KAFKA_LISTENER_PROPERTY_NAME_PREFIX = KafkaRedisReplicatorConfiguration.KAFKA_PROPERTY_NAME_PREFIX + "listener.";

    public static final String KAFKA_LISTENER_POLL_TIMEOUT_PROPERTY_NAME = KAFKA_LISTENER_PROPERTY_NAME_PREFIX + "poll-timeout";

    public static final String KAFKA_LISTENER_CONCURRENCY_PROPERTY_NAME = KAFKA_LISTENER_PROPERTY_NAME_PREFIX + "concurrency";

    public static final String KAFKA_CONSUMER_GROUP_ID_PREFIX = "Redis-Replicator-";

    public static final boolean DEFAULT_KAFKA_CONSUMER_ENABLED = true;

    public static final int DEFAULT_KAFKA_LISTENER_POLL_TIMEOUT = 10000;

    public static final int DEFAULT_KAFKA_LISTENER_CONCURRENCY = 1;

    private volatile Map<String, Object> consumerConfigs;

    /**
     * Listener connection timeout
     */
    private int listenerPollTimeOut;

    /**
     * Number of listener connection threads
     */
    private int listenerConcurrency;

    private ApplicationEventPublisher applicationEventPublisher;

    public static boolean isEnabled(ApplicationContext applicationContext) {
        return getBoolean(applicationContext, KAFKA_CONSUMER_ENABLED_PROPERTY_NAME, DEFAULT_KAFKA_CONSUMER_ENABLED, "Kafka Consumer", "enabled");
    }

    @Bean
    public ConcurrentMessageListenerContainer<byte[], byte[]> redisReplicatorConcurrentMessageListenerContainer() {
        String[] topics = getTopics();
        ContainerProperties containerProperties = new ContainerProperties(topics);
        ConsumerFactory<byte[], byte[]> redisReplicatorConsumerFactory = redisReplicatorConsumerFactory();
        ConcurrentMessageListenerContainer<byte[], byte[]> listenerContainer = new ConcurrentMessageListenerContainer<>(redisReplicatorConsumerFactory, containerProperties);
        listenerContainer.setConcurrency(getConcurrency(topics));
        listenerContainer.setupMessageListener(batchAcknowledgingMessageListener());
        return listenerContainer;
    }

    private int getConcurrency(String[] topics) {
        int topicCount = topics.length;
        return topicCount > listenerConcurrency ? topicCount : listenerConcurrency;
    }

    private BatchAcknowledgingMessageListener<byte[], byte[]> batchAcknowledgingMessageListener() {
        return (data, acknowledgment) -> {
            int size = data.size();
            for (int i = 0; i < size; i++) {
                ConsumerRecord<byte[], byte[]> consumerRecord = data.get(i);
                consumeRecord(consumerRecord);
            }
        };
    }

    private void consumeRecord(ConsumerRecord<byte[], byte[]> consumerRecord) {
        byte[] key = consumerRecord.key();
        byte[] value = consumerRecord.value();
        int partition = consumerRecord.partition();
        try {
            RedisCommandEvent redisCommandEvent = Serializers.deserialize(value, RedisCommandEvent.class);
            RedisCommandReplicatedEvent redisCommandReplicatedEvent = createRedisCommandReplicatedEvent(redisCommandEvent, consumerRecord);
            applicationEventPublisher.publishEvent(redisCommandReplicatedEvent);
            logger.debug("[Redis-Replicator-Kafka-C-S] Processing Redis Replicator message succeeded. Topic: {}, key: {}, data size: {} bytes, partition: {}", consumerRecord.topic(), key, value.length, partition);
        } catch (Throwable e) {
            logger.warn("[Redis-Replicator-Kafka-C-F] fails to process a Redis Replicator message. Topic: {}, key: {}, data size: {} bytes, partition: {}", consumerRecord.topic(), key, value.length, partition, e);
        }
    }

    private RedisCommandReplicatedEvent createRedisCommandReplicatedEvent(RedisCommandEvent redisCommandEvent, ConsumerRecord<byte[], byte[]> consumerRecord) {
        String topic = consumerRecord.topic();
        String domain = getDomain(topic);
        return new RedisCommandReplicatedEvent(redisCommandEvent, domain);
    }

    private ConsumerFactory<byte[], byte[]> redisReplicatorConsumerFactory() {
        DefaultKafkaConsumerFactory<byte[], byte[]> kafkaConsumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerConfigs());
        kafkaConsumerFactory.setKeyDeserializer(new ByteArrayDeserializer());
        kafkaConsumerFactory.setValueDeserializer(new ByteArrayDeserializer());
        return kafkaConsumerFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        initConsumerConfigs();
        initListenerConfigs();
        logger.debug("Redis Replicator Kafka consumer configuration has been initialized");
    }

    private void initConsumerConfigs() {
        Map<String, Object> consumerConfigs = new HashMap<>();

        // Kafka bootstrap servers
        consumerConfigs.put(BOOTSTRAP_SERVERS_CONFIG, brokerList);
        // Kafka consumer group id
        consumerConfigs.put(GROUP_ID_CONFIG, getConsumerGroupId());

        // Kafka Common properties
        consumerConfigs.putAll(getSubProperties(environment, KAFKA_PROPERTY_NAME_PREFIX));

        // Kafka Consumer properties
        consumerConfigs.putAll(getSubProperties(environment, KAFKA_CONSUMER_PROPERTY_NAME_PREFIX));
        this.consumerConfigs = consumerConfigs;
    }

    private void initListenerConfigs() {
        this.listenerPollTimeOut = environment.getProperty(KAFKA_LISTENER_POLL_TIMEOUT_PROPERTY_NAME, int.class, DEFAULT_KAFKA_LISTENER_POLL_TIMEOUT);
        this.listenerConcurrency = environment.getProperty(KAFKA_LISTENER_CONCURRENCY_PROPERTY_NAME, int.class, DEFAULT_KAFKA_LISTENER_CONCURRENCY);
    }

    private Map<String, Object> getConsumerConfigs() {
        return consumerConfigs;
    }

    private String getConsumerGroupId() {
        RedisConfiguration redisConfiguration = redisReplicatorConfiguration.getRedisConfiguration();
        return KAFKA_CONSUMER_GROUP_ID_PREFIX + redisConfiguration.getApplicationName();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void destroy() {
        logger.debug("Redis Replicator Kafka consumer configuration is being destroyed");
    }
}
