package io.microsphere.spring.redis.beans;

import io.microsphere.spring.redis.context.RedisContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;

import static io.microsphere.spring.redis.beans.RedisTemplateWrapper.configure;
import static io.microsphere.spring.redis.beans.RedisTemplateWrapper.newProxyRedisConnection;


/**
 * {@link StringRedisTemplate} Wrapper class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class StringRedisTemplateWrapper extends StringRedisTemplate implements DelegatingWrapper {

    private final String beanName;

    private final StringRedisTemplate delegate;

    private final RedisContext redisContext;

    public StringRedisTemplateWrapper(String beanName, StringRedisTemplate delegate, RedisContext redisContext) {
        this.beanName = beanName;
        this.delegate = delegate;
        this.redisContext = redisContext;
        init();
    }

    private void init() {
        configure(delegate, this);
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

    @Override
    public Object getDelegate() {
        return delegate;
    }
}
