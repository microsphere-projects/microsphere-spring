package io.microsphere.spring.test.redis.embedded;

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
     * No Definition
     */
    String NO_DEFINITION = "N/D";

    /**
     * @return Redis Server bean Name
     */
    String name() default "redisServer";

    /**
     * @return Redis Server port, default：6379
     */
    int port() default 6379;

    /**
     * @return Redis Server Slave addresses
     */
    String slaveOf() default NO_DEFINITION;

    /**
     * @return Redis Server config file
     */
    String configFile() default NO_DEFINITION;


    /**
     * @return Redis Server settings
     */
    String setting() default NO_DEFINITION;
}
