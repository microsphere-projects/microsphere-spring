package io.microsphere.spring.redis.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Java {@code long} or {@link Long} type {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class LongSerializer extends AbstractSerializer<Long> {

    public static final LongSerializer INSTANCE = new LongSerializer();

    @Override
    protected int calcBytesLength() {
        return LONG_BYTES_LENGTH;
    }

    @Override
    protected byte[] doSerialize(Long aLong) throws SerializationException {
        long longValue = aLong.longValue();
        byte[] bytes = new byte[]{
                (byte) longValue,
                (byte) (longValue >> 8),
                (byte) (longValue >> 16),
                (byte) (longValue >> 24),
                (byte) (longValue >> 32),
                (byte) (longValue >> 40),
                (byte) (longValue >> 48),
                (byte) (longValue >> 56)
        };
        return bytes;
    }

    @Override
    protected Long doDeserialize(byte[] bytes) throws SerializationException {
        long longValue = ((long) bytes[7] << 56)
                | ((long) bytes[6] & 0xff) << 48
                | ((long) bytes[5] & 0xff) << 40
                | ((long) bytes[4] & 0xff) << 32
                | ((long) bytes[3] & 0xff) << 24
                | ((long) bytes[2] & 0xff) << 16
                | ((long) bytes[1] & 0xff) << 8
                | ((long) bytes[0] & 0xff);
        return longValue;
    }
}
