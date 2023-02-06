package io.github.microsphere.spring.test.redis.embedded;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable the embedded Redis server
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see redis.embedded.RedisServer
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(EmbeddedRedisServerBeanDefinitionRegistrar.class)
public @interface EnableEmbeddedRedisServer {

    /**
     * 未定义
     */
    String NO_DEFINITION = "N/D";

    /**
     * @return Redis 服务器 bean 名称
     */
    String name() default "redisServer";

    /**
     * @return Redis 服务器网络端口，默认：6379
     */
    int port() default 6379;

    /**
     * @return Redis 服务器 Slave 服务器地址
     */
    String slaveOf() default NO_DEFINITION;

    /**
     * @return Redis 服务器配置文件
     */
    String configFile() default NO_DEFINITION;


    /**
     * @return Redis 服务器设置参数
     */
    String setting() default NO_DEFINITION;
}
