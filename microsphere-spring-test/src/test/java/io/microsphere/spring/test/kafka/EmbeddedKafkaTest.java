package io.microsphere.spring.test.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasKey;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasPartition;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasValue;

/**
 * {@link EmbeddedKafka}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        EmbeddedKafkaTest.class
})
@DirtiesContext
@EmbeddedKafka(partitions = 1,
        topics = {
                "test-topic"
        },
        brokerProperties = {
                "listeners = PLAINTEXT://127.0.0.1:9092",
                "port = 9092",
                "auto.create.topics.enable = true"
        })
public class EmbeddedKafkaTest {

    private static final String TEMPLATE_TOPIC = "test-topic";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Test
    public void test() throws Throwable {
        assertNotNull(kafkaTemplate);

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test", "false", embeddedKafka);
        DefaultKafkaConsumerFactory<Object, Object> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(TEMPLATE_TOPIC);
        KafkaMessageListenerContainer<Object, Object> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<Object, Object>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener(new MessageListener<Object, Object>() {

            @Override
            public void onMessage(ConsumerRecord<Object, Object> record) {
                System.out.println(record);
                records.add(record);
            }

        });

        container.setBeanName("templateTests");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());

        kafkaTemplate.send(TEMPLATE_TOPIC, "Hello,World");

        records.poll(10, TimeUnit.SECONDS);


    }


    @Test
    public void testTemplate() throws Exception {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testT", "false", embeddedKafka);
        DefaultKafkaConsumerFactory<Integer, String> cf = new DefaultKafkaConsumerFactory<Integer, String>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(TEMPLATE_TOPIC);
        KafkaMessageListenerContainer<Integer, String> container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        final BlockingQueue<ConsumerRecord<Integer, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener(new MessageListener<Integer, String>() {

            @Override
            public void onMessage(ConsumerRecord<Integer, String> record) {
                System.out.println(record);
                records.add(record);
            }

        });
        container.setBeanName("templateTests");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
//        Map<String, Object> producerProps =
//                KafkaTestUtils.producerProps(embeddedKafka);
//        ProducerFactory<Integer, String> pf =
//                new DefaultKafkaProducerFactory<Integer, String>(producerProps);
//        KafkaTemplate<Integer, String> template = new KafkaTemplate<>(pf);

        kafkaTemplate.setDefaultTopic(TEMPLATE_TOPIC);
        kafkaTemplate.sendDefault("foo");
        assertThat(records.poll(10, TimeUnit.SECONDS), hasValue("foo"));
        kafkaTemplate.sendDefault(0, 2, "bar");
        ConsumerRecord<Integer, String> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received, hasKey(2));
        assertThat(received, hasPartition(0));
        assertThat(received, hasValue("bar"));
        kafkaTemplate.send(TEMPLATE_TOPIC, 0, 2, "baz");
        received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received, hasKey(2));
        assertThat(received, hasPartition(0));
        assertThat(received, hasValue("baz"));

        kafkaTemplate.sendDefault("Hello,World");
        records.poll(10, TimeUnit.SECONDS);
    }

    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate(EmbeddedKafkaBroker embeddedKafka) {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        ProducerFactory<Integer, String> pf = new DefaultKafkaProducerFactory<Integer, String>(producerProps);
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate(pf);
        kafkaTemplate.setDefaultTopic(TEMPLATE_TOPIC);
        return kafkaTemplate;
    }
}
