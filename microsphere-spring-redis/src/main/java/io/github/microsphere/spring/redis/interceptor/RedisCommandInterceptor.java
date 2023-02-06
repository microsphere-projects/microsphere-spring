package io.github.microsphere.spring.redis.interceptor;

import org.springframework.data.redis.connection.RedisCommands;

/**
 * {@link RedisCommands Redis Command} interceptor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface RedisCommandInterceptor extends RedisMethodInterceptor<RedisCommands> {
}
