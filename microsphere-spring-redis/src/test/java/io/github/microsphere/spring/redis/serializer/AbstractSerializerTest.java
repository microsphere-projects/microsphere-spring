package io.github.microsphere.spring.redis.serializer;

import org.junit.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.springframework.core.ResolvableType.forType;

/**
 * Abstract {@link RedisSerializer} Test
 *
 * @param <T> Serialization type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractSerializerTest<T> {

    @Test
    public void test() {
        test(this::getValue);
    }

    @Test
    public void testNull() {
        test(this::getNullValue);
    }

    public void test(Supplier<T> valueSupplier) {
        T value = valueSupplier.get();
        RedisSerializer<T> serializer = getSerializer();
        byte[] bytes = serializer.serialize(value);
        T deserialized = serializer.deserialize(bytes);
        if (value != null && deserialized != null) {
            assertEquals(getTestData(value), getTestData(deserialized));
        } else {
            assertEquals(value, deserialized);
        }

        Class<?> targetType = serializer.getTargetType();
        Class<?> parameterType = forType(getClass()).getSuperType().getGeneric(0).resolve();
        assertSame(targetType, parameterType);
        assertTrue(serializer.canSerialize(parameterType));

        if (serializer instanceof AbstractSerializer) {
            AbstractSerializer abstractSerializer = (AbstractSerializer) serializer;
            assertSame(targetType, abstractSerializer.getParameterizedClass());
        }
    }

    protected Object getTestData(T value) {
        return value;
    }

    protected abstract RedisSerializer<T> getSerializer();

    protected abstract T getValue();

    protected T getNullValue() {
        return null;
    }
}
