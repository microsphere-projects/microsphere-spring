package io.microsphere.spring.redis.beans;

import io.microsphere.spring.redis.context.RedisContext;
import io.microsphere.spring.redis.interceptor.InterceptingRedisConnectionInvocationHandler;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * {@link RedisTemplate} Wrapper class, compatible with {@link RedisTemplate}
 *
 * @param <K> Redis Key type
 * @param <V> Redis Value type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RedisTemplateWrapper<K, V> extends RedisTemplate<K, V> implements DelegatingWrapper {

    private static final Class<?>[] REDIS_CONNECTION_TYPES = new Class[]{RedisConnection.class};

    private final String beanName;

    private final RedisTemplate<K, V> delegate;

    private final RedisContext redisContext;

    public RedisTemplateWrapper(String beanName, RedisTemplate<K, V> delegate, RedisContext redisContext) {
        this.beanName = beanName;
        this.delegate = delegate;
        this.redisContext = redisContext;
        init();
    }

    private void init() {
        configure(delegate, this);
    }

    static void configure(RedisTemplate<?, ?> source, RedisTemplate<?, ?> target) {
        // Set the connection
        target.setConnectionFactory(source.getConnectionFactory());
        target.setExposeConnection(source.isExposeConnection());

        // Set the RedisSerializers
        target.setEnableDefaultSerializer(source.isEnableDefaultSerializer());
        target.setDefaultSerializer(source.getDefaultSerializer());
        target.setKeySerializer(source.getKeySerializer());
        target.setValueSerializer(source.getValueSerializer());
        target.setHashKeySerializer(source.getHashKeySerializer());
        target.setHashValueSerializer(source.getHashValueSerializer());
        target.setStringSerializer(source.getStringSerializer());
    }

    @Override
    protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
        if (isEnabled()) {
            return newProxyRedisConnection(connection, redisContext, beanName);
        }
        return connection;
    }

    public boolean isEnabled() {
        return redisContext.isEnabled();
    }

    public String getBeanName() {
        return beanName;
    }

    public RedisContext getRedisContext() {
        return redisContext;
    }

    protected static RedisConnection newProxyRedisConnection(RedisConnection connection, RedisContext redisContext, String sourceBeanName) {
        ClassLoader classLoader = redisContext.getClassLoader();
        InvocationHandler invocationHandler = newInvocationHandler(connection, redisContext, sourceBeanName);
        return (RedisConnection) Proxy.newProxyInstance(classLoader, REDIS_CONNECTION_TYPES, invocationHandler);
    }

    private static InvocationHandler newInvocationHandler(RedisConnection connection, RedisContext redisContext, String redisTemplateBeanName) {
        return new InterceptingRedisConnectionInvocationHandler(connection, redisContext, redisTemplateBeanName);
    }

    @Override
    public Object getDelegate() {
        return delegate;
    }
}
