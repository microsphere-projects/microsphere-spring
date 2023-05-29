package io.microsphere.spring.test.kafka;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable Kafka Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EmbeddedKafka
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EmbeddedKafka
@Import(KafkaTestConfiguration.class)
public @interface EnableKafkaTest {

    /**
     * @return the number of brokers
     */
    @AliasFor(value = "value", annotation = EmbeddedKafka.class)
    int brokers() default 1;

    /**
     * @return passed into {@code kafka.utils.TestUtils.createBrokerConfig()}.
     */
    @AliasFor(value = "controlledShutdown", annotation = EmbeddedKafka.class)
    boolean controlledShutdown() default false;

    /**
     * Set explicit ports on which the kafka brokers will listen. Useful when running an
     * embedded broker that you want to access from other processes.
     * A port must be provided for each instance, which means the number of ports must match the value of the count attribute.
     *
     * @return ports for brokers.
     * @since 2.2.4
     */
    @AliasFor(value = "ports", annotation = EmbeddedKafka.class)
    int[] ports() default {9092};

    /**
     * @return partitions per topic
     */
    @AliasFor(value = "partitions", annotation = EmbeddedKafka.class)
    int partitions() default 1;

    /**
     * Topics that should be created Topics may contain property placeholders, e.g.
     * {@code topics = "${kafka.topic.one:topicOne}"} The topics will be created with
     * {@link #partitions()} partitions; to provision other topics with other partition
     * counts call the {@code addTopics(NewTopic... topics)} method on the autowired
     * broker.
     *
     * @return the topics to create
     */
    @AliasFor(value = "topics", annotation = EmbeddedKafka.class)
    String[] topics() default {};

    /**
     * Properties in form {@literal key=value} that should be added to the broker config
     * before runs. Properties may contain property placeholders, e.g.
     * {@code delete.topic.enable=${topic.delete:true}}.
     *
     * @return the properties to add
     * @see #brokerPropertiesLocation()
     * @see org.springframework.kafka.test.EmbeddedKafkaBroker#brokerProperties(java.util.Map)
     */
    @AliasFor(value = "brokerProperties", annotation = EmbeddedKafka.class)
    String[] brokerProperties() default {
            "listeners = PLAINTEXT://127.0.0.1:9092",
            "auto.create.topics.enable = true"
    };

    /**
     * Spring {@code Resource} url specifying the location of properties that should be
     * added to the broker config. The {@code brokerPropertiesLocation} url and the
     * properties themselves may contain placeholders that are resolved during
     * initialization. Properties specified by {@link #brokerProperties()} will override
     * properties found in {@code brokerPropertiesLocation}.
     *
     * @return a {@code Resource} url specifying the location of properties to add
     * @see #brokerProperties()
     * @see org.springframework.kafka.test.EmbeddedKafkaBroker#brokerProperties(java.util.Map)
     */
    @AliasFor(value = "brokerPropertiesLocation", annotation = EmbeddedKafka.class)
    String brokerPropertiesLocation() default "";
}
