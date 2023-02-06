package io.github.microsphere.spring.redis.serializer;

import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * {@link RedisStringCommands.SetOption} {@link EnumSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class SetOptionSerializerTest extends AbstractSerializerTest<RedisStringCommands.SetOption> {

    @Override
    protected RedisSerializer<RedisStringCommands.SetOption> getSerializer() {
        return new EnumSerializer(RedisStringCommands.SetOption.class);
    }

    @Override
    protected RedisStringCommands.SetOption getValue() {
        return RedisStringCommands.SetOption.SET_IF_ABSENT;
    }
}
