package io.github.microsphere.spring.redis.serializer;

import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.concurrent.TimeUnit;

/**
 * {@link Expiration} {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ExpirationSerializer extends AbstractSerializer<Expiration> {

    private static final LongSerializer longSerializer = LongSerializer.INSTANCE;

    private static final EnumSerializer<TimeUnit> timeUnitEnumSerializer = new EnumSerializer<>(TimeUnit.class);

    private static final int expirationTimeBytesLength = longSerializer.getBytesLength();

    private static final int timeUnitBytesLength = timeUnitEnumSerializer.getBytesLength();

    public static final ExpirationSerializer INSTANCE = new ExpirationSerializer();

    @Override
    protected int calcBytesLength() {
        return expirationTimeBytesLength + timeUnitBytesLength;
    }

    @Override
    protected byte[] doSerialize(Expiration expiration) throws SerializationException {

        int bytesLength = getBytesLength();

        long expirationTime = expiration.getExpirationTime();

        byte[] expirationTimeBytes = longSerializer.serialize(expirationTime);

        byte[] bytes = new byte[bytesLength];

        int i = 0;
        for (; i < expirationTimeBytesLength; i++) {
            bytes[i] = expirationTimeBytes[i];
        }

        TimeUnit timeUnit = expiration.getTimeUnit();
        byte ordinal = (byte) timeUnit.ordinal();

        for (; i < bytesLength; i++) {
            bytes[i] = ordinal;
        }

        return bytes;
    }

    @Override
    protected Expiration doDeserialize(byte[] bytes) throws SerializationException {
        int expirationTimeBytesLength = this.expirationTimeBytesLength;
        int timeUnitBytesLength = this.timeUnitBytesLength;
        int bytesLength = this.getBytesLength();

        // ExpirationTime array
        byte[] expirationTimeBytes = new byte[expirationTimeBytesLength];
        int i = 0;
        for (; i < expirationTimeBytesLength; i++) {
            expirationTimeBytes[i] = bytes[i];
        }

        // TimeUnit array
        byte[] timeUnitBytes = new byte[timeUnitBytesLength];
        for (int j = 0; j < timeUnitBytesLength && i < bytesLength; j++, i++) {
            timeUnitBytes[j] = bytes[i];
        }

        long expirationTime = longSerializer.deserialize(expirationTimeBytes);

        TimeUnit timeUnit = timeUnitEnumSerializer.deserialize(timeUnitBytes);

        return Expiration.from(expirationTime, timeUnit);
    }

}
