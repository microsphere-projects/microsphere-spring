package io.github.microsphere.spring.test.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link RedisTestConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@EnableRedisTest
@ContextConfiguration(classes = RedisTestConfigurationTest.class)
public class RedisTestConfigurationTest {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test() {
        assertTrue(redisTemplate.opsForValue().setIfAbsent("Hello", "World"));
        assertEquals("World", redisTemplate.opsForValue().get("Hello"));
        assertTrue(stringRedisTemplate.opsForValue().setIfPresent("Hello", "World"));
        assertEquals("World", stringRedisTemplate.opsForValue().get("Hello"));
    }

}
