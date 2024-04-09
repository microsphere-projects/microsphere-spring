package io.microsphere.spring.test.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasKey;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasPartition;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasValue;

/**
 * {@link EnableKafkaTest} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        EnableKafkaTestCase.class
})
@DirtiesContext
@EnableKafkaTest(topics = {
        "test-topic"
})
public class EnableKafkaTestCase {

    private static final String TEMPLATE_TOPIC = "topic-EnableKafkaTestCase";

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private CompositeMessageListener<Object, Object> compositeMessageListener;

    @Test
    public void test() throws Exception {

        final BlockingQueue<ConsumerRecord<Object, Object>> records = new LinkedBlockingQueue<>();
        compositeMessageListener.addMessageListener(new MessageListener<Object, Object>() {

            @Override
            public void onMessage(ConsumerRecord<Object, Object> record) {
                System.out.println(record);
                records.add(record);
            }
        });

        kafkaTemplate.setDefaultTopic(TEMPLATE_TOPIC);
        kafkaTemplate.sendDefault("foo");
        assertThat(records.poll(10, TimeUnit.SECONDS), hasValue("foo"));
        kafkaTemplate.sendDefault(0, 2, "bar");
        ConsumerRecord<Object, Object> received = records.poll(10, TimeUnit.SECONDS);
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
}
