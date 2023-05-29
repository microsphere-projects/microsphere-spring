package io.microsphere.spring.redis.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Java {@code boolean} or {@link Boolean} type {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class BooleanSerializer extends AbstractSerializer<Boolean> {

    public static final BooleanSerializer INSTANCE = new BooleanSerializer();

    private static final byte NULL_VALUE = -1;

    private static final byte TRUE_VALUE = 1;

    private static final byte FALSE_VALUE = 0;

    @Override
    protected int calcBytesLength() {
        return BOOLEAN_BYTES_LENGTH;
    }

    @Override
    protected byte[] doSerialize(Boolean booleanValue) throws SerializationException {
        byte byteValue = booleanValue == null ? NULL_VALUE : (booleanValue ? TRUE_VALUE : FALSE_VALUE);
        byte[] bytes = new byte[]{byteValue};
        return bytes;
    }

    @Override
    protected Boolean doDeserialize(byte[] bytes) throws SerializationException {
        Boolean booleanValue = null;
        byte byteValue = bytes[0];
        switch (byteValue) {
            case NULL_VALUE:
                booleanValue = null;
                break;
            case TRUE_VALUE:
                booleanValue = Boolean.TRUE;
                break;
            case FALSE_VALUE:
                booleanValue = Boolean.FALSE;
                break;
        }
        return booleanValue;
    }

}
