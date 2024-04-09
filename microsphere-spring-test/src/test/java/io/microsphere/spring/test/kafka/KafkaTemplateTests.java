package io.microsphere.spring.test.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasKey;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasPartition;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasValue;

/**
 * TODO Comment
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since TODO
 */
public class KafkaTemplateTests {

    private static final String TEMPLATE_TOPIC = "topic-KafkaTemplateTests";

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, TEMPLATE_TOPIC);

    @Test
    public void testTemplate() throws Exception {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testT", "false",
                embeddedKafka.getEmbeddedKafka());
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
        ContainerTestUtils.waitForAssignment(container,
                embeddedKafka.getEmbeddedKafka().getPartitionsPerTopic());
        Map<String, Object> producerProps =
                KafkaTestUtils.producerProps(embeddedKafka.getEmbeddedKafka());
        ProducerFactory<Integer, String> pf =
                new DefaultKafkaProducerFactory<Integer, String>(producerProps);
        KafkaTemplate<Integer, String> template = new KafkaTemplate<>(pf);
        template.setDefaultTopic(TEMPLATE_TOPIC);
        template.sendDefault("foo");
        assertThat(records.poll(10, TimeUnit.SECONDS), hasValue("foo"));
        template.sendDefault(0, 2, "bar");
        ConsumerRecord<Integer, String> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received, hasKey(2));
        assertThat(received, hasPartition(0));
        assertThat(received, hasValue("bar"));
        template.send(TEMPLATE_TOPIC, 0, 2, "baz");
        received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received, hasKey(2));
        assertThat(received, hasPartition(0));
        assertThat(received, hasValue("baz"));
    }

}