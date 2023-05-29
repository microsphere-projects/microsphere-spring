package io.microsphere.spring.redis.serializer;

import org.junit.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * {@link BooleanSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class BooleanSerializerTest extends AbstractSerializerTest<Boolean> {

    @Override
    protected RedisSerializer<Boolean> getSerializer() {
        return BooleanSerializer.INSTANCE;
    }

    @Override
    protected Boolean getValue() {
        return true;
    }

    @Test
    public void testFalse() {
        test(this::falseValue);
    }

    private Boolean falseValue() {
        return Boolean.FALSE;
    }
}
