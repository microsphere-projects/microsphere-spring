package io.github.microsphere.spring.redis.replicator;

import io.github.microsphere.spring.redis.event.RedisCommandEvent;
import io.github.microsphere.spring.redis.replicator.event.RedisCommandReplicatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.function.Function;

import static io.github.microsphere.spring.redis.beans.Wrapper.tryUnwrap;
import static io.github.microsphere.spring.redis.metadata.RedisMetadataRepository.findWriteCommandMethod;
import static io.github.microsphere.spring.redis.metadata.RedisMetadataRepository.getRedisCommandBindingFunction;


/**
 * Redis Command Replicator
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RedisCommandReplicator implements ApplicationListener<RedisCommandReplicatedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RedisCommandReplicator.class);

    public static final String BEAN_NAME = "redisCommandReplicator";

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisCommandReplicator(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = tryUnwrap(redisConnectionFactory, RedisConnectionFactory.class);
    }

    @Override
    public void onApplicationEvent(RedisCommandReplicatedEvent event) {
        try {
            handleRedisCommandEvent(event);
        } catch (Throwable e) {
            logger.error("[Redis-Replicator-Event] Failed to process Redis command event [{}]", event, e);
        }
    }

    private void handleRedisCommandEvent(RedisCommandReplicatedEvent event) throws Throwable {
        RedisCommandEvent redisCommandEvent = event.getSourceEvent();
        Method method = findWriteCommandMethod(redisCommandEvent);
        if (method != null) {
            String interfaceNme = redisCommandEvent.getInterfaceName();
            RedisConnection redisConnection = getRedisConnection();
            Object[] args = redisCommandEvent.getArgs();
            Function<RedisConnection, Object> bindingFunction = getRedisCommandBindingFunction(interfaceNme);
            Object redisCommandObject = bindingFunction.apply(redisConnection);
            // TODO: Native method implementation
            ReflectionUtils.invokeMethod(method, redisCommandObject, args);
        }
    }

    private RedisConnection getRedisConnection() {
        return redisConnectionFactory.getConnection();
    }
}
