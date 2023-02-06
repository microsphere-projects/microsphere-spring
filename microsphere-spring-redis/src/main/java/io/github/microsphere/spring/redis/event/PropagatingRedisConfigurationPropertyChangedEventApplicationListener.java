package io.github.microsphere.spring.redis.event;

import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.SmartApplicationListener;

import java.util.Set;
import java.util.stream.Collectors;

import static io.github.microsphere.spring.redis.util.RedisConstants.PROPERTY_NAME_PREFIX;

/**
 * {@link EnvironmentChangeEvent} {@link ApplicationListener} propagates {@link RedisConfigurationPropertyChangedEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisConfigurationPropertyChangedEvent
 * @since 1.0.0
 */
public class PropagatingRedisConfigurationPropertyChangedEventApplicationListener implements SmartApplicationListener {

    public static final String ENVIRONMENT_CHANGE_EVENT_CLASS_NAME = "org.springframework.cloud.context.environment.EnvironmentChangeEvent";

    private final ConfigurableApplicationContext context;

    public PropagatingRedisConfigurationPropertyChangedEventApplicationListener(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ENVIRONMENT_CHANGE_EVENT_CLASS_NAME.equals(eventType.getName());
    }

    @Override
    public void onApplicationEvent(ApplicationEvent e) {
        EnvironmentChangeEvent environmentChangeEvent = (EnvironmentChangeEvent) e;
        Set<String> keys = environmentChangeEvent.getKeys().stream().filter(this::isRedisPropertyName).collect(Collectors.toSet());
        RedisConfigurationPropertyChangedEvent event = new RedisConfigurationPropertyChangedEvent(context, keys);
        context.publishEvent(event);
    }

    private boolean isRedisPropertyName(String key) {
        return key != null && key.startsWith(PROPERTY_NAME_PREFIX);
    }

}
