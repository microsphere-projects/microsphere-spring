package io.github.microsphere.spring.redis.serializer;

import org.springframework.core.ResolvableType;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import static org.springframework.core.ResolvableType.forType;

/**
 * Abstract {@link RedisSerializer} Class
 *
 * @param <T> Serialized/Deserialized type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractSerializer<T> implements RedisSerializer<T> {

    public static final int UNBOUND_BYTES_LENGTH = -1;

    public static final int BOOLEAN_BYTES_LENGTH = 1;

    public static final int BYTE_BYTES_LENGTH = 1;

    public static final int SHORT_BYTES_LENGTH = 2;

    public static final int INTEGER_BYTES_LENGTH = 4;

    public static final int FLOAT_BYTES_LENGTH = 4;

    public static final int LONG_BYTES_LENGTH = 8;

    public static final int DOUBLE_BYTES_LENGTH = 8;

    private final ResolvableType type;

    private final Class<T> targetType;

    private final int bytesLength;

    public AbstractSerializer() {
        this.type = resolvableType();
        this.targetType = (Class<T>) type.resolve();
        this.bytesLength = calcBytesLength();
    }

    @Override
    public final byte[] serialize(T t) throws SerializationException {
        // null compatible case
        if (t == null) {
            return null;
        }

        return doSerialize(t);
    }

    @Override
    public final T deserialize(byte[] bytes) throws SerializationException {
        // null compatible case
        if (bytes == null) {
            return null;
        }

        // Compatible byte array fixed case
        if (bytesLength != UNBOUND_BYTES_LENGTH && bytes.length != getBytesLength()) {
            return null;
        }

        return doDeserialize(bytes);
    }

    @Override
    public final Class<T> getTargetType() {
        return targetType;
    }

    public ResolvableType getParameterizedType() {
        return type;
    }

    public Class<T> getParameterizedClass() {
        return getTargetType();
    }

    public int getBytesLength() {
        return bytesLength;
    }

    protected int calcBytesLength() {
        return UNBOUND_BYTES_LENGTH;
    }

    protected abstract byte[] doSerialize(T t) throws SerializationException;

    protected abstract T doDeserialize(byte[] bytes) throws SerializationException;

    private ResolvableType resolvableType() {
        return forType(getClass()).as(RedisSerializer.class).getGeneric(0);
    }
}
