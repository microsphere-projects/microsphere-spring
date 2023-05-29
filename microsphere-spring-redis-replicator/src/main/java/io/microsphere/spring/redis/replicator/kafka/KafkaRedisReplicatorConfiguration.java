package io.microsphere.spring.redis.replicator.kafka;

import io.microsphere.spring.redis.replicator.config.RedisReplicatorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.List;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;


/**
 * Kafka {@link RedisReplicatorConfiguration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisReplicatorConfiguration
 * @since 1.0.0
 */
public class KafkaRedisReplicatorConfiguration implements EnvironmentAware, InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(KafkaRedisReplicatorConfiguration.class);

    public static final String SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME = "spring.kafka.bootstrap-servers";

    public static final String KAFKA_PROPERTY_NAME_PREFIX = RedisReplicatorConfiguration.PROPERTY_NAME_PREFIX + "kafka.";

    public static final String KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME = KAFKA_PROPERTY_NAME_PREFIX + BOOTSTRAP_SERVERS_CONFIG;

    public static final String KAFKA_BOOTSTRAP_SERVERS_PROPERTY_PLACEHOLDER = "${" + KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME + ":${" + SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME + "}}";

    public static final String KAFKA_TOPIC_PREFIX_PROPERTY_NAME = KAFKA_PROPERTY_NAME_PREFIX + "topic-prefix";

    public static final String DEFAULT_KAFKA_TOPIC_PREFIX_PROPERTY_VALUE = "redis-replicator-event-topic-";

    /**
     * Node ip Port address (reusing application configurations)
     */
    protected String brokerList;

    protected String topicPrefix;

    protected ConfigurableEnvironment environment;

    @Autowired
    protected RedisReplicatorConfiguration redisReplicatorConfiguration;

    protected String[] topics;

    public void initTopics() {
        List<String> domains = redisReplicatorConfiguration.getDomains();
        int size = domains.size();
        String[] topics = new String[size];
        for (int i = 0; i < size; i++) {
            String domain = domains.get(i);
            String topic = createTopic(domain);
            topics[i] = topic;
        }
        this.topics = topics;
    }

    public String createTopic(String domain) {
        return topicPrefix + domain;
    }

    public String getDomain(String topic) {
        if (topic == null) {
            return null;
        }
        int index = topic.indexOf(topicPrefix);
        if (index > -1) {
            return topic.substring(topicPrefix.length());
        }
        return null;
    }

    public String[] getTopics() {
        return topics;
    }

    @Override
    public final void setEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment, "The 'environment' argument is not an instance of ConfigurableEnvironment");
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initBrokerList();
        initTopicPrefix();
        initTopics();
    }

    private void initBrokerList() {
        String brokerList = environment.resolvePlaceholders(KAFKA_BOOTSTRAP_SERVERS_PROPERTY_PLACEHOLDER);
        logger.debug("Kafka Broker list : {}", brokerList);
        this.brokerList = brokerList;
    }

    private void initTopicPrefix() {
        String topicPrefix = environment.getProperty(KAFKA_TOPIC_PREFIX_PROPERTY_NAME, DEFAULT_KAFKA_TOPIC_PREFIX_PROPERTY_VALUE);
        logger.debug("Kafka Topic prefix : {}", topicPrefix);
        this.topicPrefix = topicPrefix;
    }

    @Override
    public void destroy() throws Exception {
    }
}
