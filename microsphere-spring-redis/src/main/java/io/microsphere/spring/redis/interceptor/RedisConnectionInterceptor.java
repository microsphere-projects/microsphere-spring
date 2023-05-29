package io.microsphere.spring.redis.interceptor;

import org.springframework.data.redis.connection.RedisConnection;

/**
 * {@link RedisConnection} interceptor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface RedisConnectionInterceptor extends RedisMethodInterceptor<RedisConnection> {
}
