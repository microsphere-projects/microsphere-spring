package io.github.microsphere.spring.test.redis.embedded;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import redis.embedded.RedisServer;

import static org.junit.Assert.assertTrue;

/**
 * {@link EnableEmbeddedRedisServer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = EnableEmbeddedRedisServerTest.class)
@EnableEmbeddedRedisServer
public class EnableEmbeddedRedisServerTest {

    @Autowired
    private RedisServer redisServer;

    @Test
    public void test(){
       assertTrue(redisServer.isActive());
    }
}
