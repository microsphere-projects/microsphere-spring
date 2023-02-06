package io.github.microsphere.spring.test.redis;

import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.embedded.RedisServer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable Redis Test configures the component list:
 * <ul>
 *     <li>{@link RedisTemplate RedisTemplate&lt;Object,Object&gt;}</li>
 *     <li>{@link StringRedisTemplate}</li>
 *     <li>{@link RedisServer}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(RedisTestConfiguration.class)
public @interface EnableRedisTest {
}
