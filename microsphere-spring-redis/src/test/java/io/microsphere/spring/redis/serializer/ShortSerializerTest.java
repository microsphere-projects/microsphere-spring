package io.microsphere.spring.redis.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * {@link ShortSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ShortSerializerTest extends AbstractSerializerTest<Short> {

    @Override
    protected RedisSerializer<Short> getSerializer() {
        return ShortSerializer.INSTANCE;
    }

    @Override
    protected Short getValue() {
        return 128;
    }
}
