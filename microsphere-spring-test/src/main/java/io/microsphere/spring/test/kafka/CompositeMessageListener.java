package io.microsphere.spring.test.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.LinkedList;
import java.util.List;

/**
 * Composite {@link MessageListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class CompositeMessageListener<K, V> implements MessageListener<K, V> {

    private final List<MessageListener<K, V>> messageListeners = new LinkedList<>();

    public CompositeMessageListener<K, V> addMessageListener(MessageListener<K, V> messageListener) {
        if (messageListener != this) {
            this.messageListeners.add(messageListener);
        }
        return this;
    }

    public CompositeMessageListener<K, V> addMessageListeners(Iterable<MessageListener<K, V>> messageListeners) {
        messageListeners.forEach(this::addMessageListener);
        return this;
    }

    @Override
    public void onMessage(ConsumerRecord<K, V> data) {
        messageListeners.forEach(listener -> {
            listener.onMessage(data);
        });
    }

    @Override
    public void onMessage(ConsumerRecord<K, V> data, Acknowledgment acknowledgment) {
        messageListeners.forEach(listener -> {
            listener.onMessage(data, acknowledgment);
        });
    }

    @Override
    public void onMessage(ConsumerRecord<K, V> data, Consumer<?, ?> consumer) {
        messageListeners.forEach(listener -> {
            listener.onMessage(data, consumer);
        });
    }

    @Override
    public void onMessage(ConsumerRecord<K, V> data, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {
        messageListeners.forEach(listener -> {
            listener.onMessage(data, acknowledgment, consumer);
        });
    }
}
