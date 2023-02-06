package io.github.microsphere.spring.redis.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * {@link LongSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class LongSerializerTest extends AbstractSerializerTest<Long> {

    @Override
    protected RedisSerializer<Long> getSerializer() {
        return LongSerializer.INSTANCE;
    }

    @Override
    protected Long getValue() {
        return 1234567890L;
    }
}
