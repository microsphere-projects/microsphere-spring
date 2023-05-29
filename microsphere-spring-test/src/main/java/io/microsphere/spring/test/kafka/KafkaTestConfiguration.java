package io.microsphere.spring.test.kafka;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.LoggingProducerListener;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;
import java.util.StringJoiner;

/**
 * Kafka Test configuration
 *
 * @param <K> Key type
 * @param <V> Value type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class KafkaTestConfiguration<K, V> {

    private final RecordMessageConverter messageConverter;

    private final ProducerListener<K, V> kafkaProducerListener;

    public KafkaTestConfiguration(ObjectProvider<RecordMessageConverter> messageConverter,
                                  ObjectProvider<ProducerListener<K, V>> kafkaProducerListener) {
        this.messageConverter = messageConverter.getIfAvailable();
        this.kafkaProducerListener = kafkaProducerListener.getIfAvailable(LoggingProducerListener::new);
    }

    @Bean
    public KafkaTemplate<K, V> kafkaTemplate(EmbeddedKafkaBroker embeddedKafka) {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        ProducerFactory<K, V> pf = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<K, V> kafkaTemplate = new KafkaTemplate(pf);
        if (messageConverter != null) {
            kafkaTemplate.setMessageConverter(messageConverter);
        }
        kafkaTemplate.setProducerListener(kafkaProducerListener);
        return kafkaTemplate;
    }

    @Bean
    public GenericMessageListenerContainer<K, V> messageListenerContainer(EmbeddedKafkaBroker embeddedKafka,
                                                                          CompositeMessageListener<K, V> compositeMessageListener) {
        String[] topics = embeddedKafka.getTopics().toArray(new String[0]);
        StringJoiner nameBuilder = new StringJoiner("-");
        nameBuilder.add("group");
        for (String topic : topics) {
            nameBuilder.add(topic);
        }
        String beanName = nameBuilder.toString();
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(beanName, "false", embeddedKafka);
        DefaultKafkaConsumerFactory<K, V> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(topics);
        KafkaMessageListenerContainer<K, V> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        container.setBeanName(beanName);
        container.setupMessageListener(compositeMessageListener);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
        return container;
    }

    @Bean
    @Primary
//    @Autowired(required = false)
    public CompositeMessageListener<K, V> compositeMessageListener() {
        CompositeMessageListener<K, V> compositeMessageListener = new CompositeMessageListener<>();
//        compositeMessageListener.addMessageListeners(messageListeners);
        return compositeMessageListener;
    }

}
