package io.github.microsphere.spring.redis.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Java {@code double} or {@link Double} type {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class DoubleSerializer extends AbstractSerializer<Double> {

    private static final LongSerializer longSerializer = LongSerializer.INSTANCE;

    public static final DoubleSerializer INSTANCE = new DoubleSerializer();


    @Override
    protected int calcBytesLength() {
        return DOUBLE_BYTES_LENGTH;
    }

    @Override
    protected byte[] doSerialize(Double aDouble) throws SerializationException {
        double doubleValue = aDouble.doubleValue();
        long longValue = Double.doubleToLongBits(doubleValue);
        return longSerializer.serialize(longValue);
    }

    @Override
    protected Double doDeserialize(byte[] bytes) throws SerializationException {
        long longValue = longSerializer.deserialize(bytes);
        double doubleValue = Double.longBitsToDouble(longValue);
        return doubleValue;
    }
}
