package io.microsphere.spring.redis.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * {@link DoubleSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class DoubleSerializerTest extends AbstractSerializerTest<Double> {

    @Override
    protected RedisSerializer<Double> getSerializer() {
        return DoubleSerializer.INSTANCE;
    }

    @Override
    protected Double getValue() {
        return 123456.789;
    }
}
