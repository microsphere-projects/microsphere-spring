package io.microsphere.spring.redis.serializer;

import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * {@link WeightsSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class WeightsSerializerTest extends AbstractSerializerTest<RedisZSetCommands.Weights> {

    @Override
    protected RedisSerializer<RedisZSetCommands.Weights> getSerializer() {
        return WeightsSerializer.INSTANCE;
    }

    @Override
    protected RedisZSetCommands.Weights getValue() {
        return RedisZSetCommands.Weights.of(1.0, 2.0, 3.0);
    }

    @Override
    protected Object getTestData(RedisZSetCommands.Weights value) {
        return value.toList();
    }
}
