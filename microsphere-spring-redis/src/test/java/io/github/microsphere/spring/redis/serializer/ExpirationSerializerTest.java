package io.github.microsphere.spring.redis.serializer;

import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.concurrent.TimeUnit;

/**
 * {@link ExpirationSerializer}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ExpirationSerializerTest extends AbstractSerializerTest<Expiration> {

    @Override
    protected RedisSerializer<Expiration> getSerializer() {
        return ExpirationSerializer.INSTANCE;
    }

    @Override
    protected Expiration getValue() {
        return Expiration.from(1, TimeUnit.SECONDS);
    }

    protected Object getTestData(Expiration value) {
        return value.getExpirationTimeInMilliseconds();
    }
}
