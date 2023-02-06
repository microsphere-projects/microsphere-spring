package io.github.microsphere.spring.redis.serializer;

import org.springframework.data.redis.connection.DefaultSortParameters;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.HashMap;
import java.util.Map;

import static io.github.microsphere.spring.redis.serializer.Serializers.defaultSerialize;

/**
 * {@link SortParameters} {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DefaultSortParameters
 * @since 1.0.0
 */
public class SortParametersSerializer extends AbstractSerializer<SortParameters> {

    private static final String ORDER_KEY = "o";

    private static final String ALPHABETIC_KEY = "a";

    private static final String BY_PATTERN_KEY = "b";

    private static final String GET_PATTERN_KEY = "g";

    private static final String LIMIT_KEY = "l";

    private static final EnumSerializer orderEnumSerializer = new EnumSerializer(SortParameters.Order.class);

    private static final BooleanSerializer booleanSerializer = BooleanSerializer.INSTANCE;

    private static final ByteArraySerializer byteArraySerializer = ByteArraySerializer.INSTANCE;

    private static final RangeSerializer rangeSerializer = RangeSerializer.INSTANCE;

    public static final SortParametersSerializer INSTANCE = new SortParametersSerializer();

    @Override
    protected byte[] doSerialize(SortParameters sortParameters) throws SerializationException {
        Map<String, Object> data = new HashMap<>(5);

        SortParameters.Order order = sortParameters.getOrder();
        if (order != null) {
            data.put(ORDER_KEY, orderEnumSerializer.serialize(order));
        }

        Boolean alphabetic = sortParameters.isAlphabetic();
        if (alphabetic != null) {
            data.put(ALPHABETIC_KEY, booleanSerializer.serialize(alphabetic));
        }

        byte[] byPattern = sortParameters.getByPattern();
        if (byPattern != null) {
            data.put(BY_PATTERN_KEY, byteArraySerializer.serialize(byPattern));
        }

        byte[][] getPattern = sortParameters.getGetPattern();
        if (getPattern != null) {
            data.put(GET_PATTERN_KEY, defaultSerialize(getPattern));
        }

        SortParameters.Range limit = sortParameters.getLimit();
        if (limit != null) {
            data.put(LIMIT_KEY, rangeSerializer.serialize(limit));
        }

        return defaultSerialize(data);
    }

    @Override
    protected SortParameters doDeserialize(byte[] bytes) throws SerializationException {
        DefaultSortParameters sortParameters = new DefaultSortParameters();

        Map<String, Object> data = Serializers.deserialize(bytes, Map.class);

        byte[] orderBytes = (byte[]) data.get(ORDER_KEY);
        sortParameters.setOrder((SortParameters.Order) orderEnumSerializer.deserialize(orderBytes));

        byte[] alphabeticBytes = (byte[]) data.get(ALPHABETIC_KEY);
        sortParameters.setAlphabetic(booleanSerializer.deserialize(alphabeticBytes));

        byte[] byPatternBytes = (byte[]) data.get(BY_PATTERN_KEY);
        sortParameters.setByPattern(byteArraySerializer.deserialize(byPatternBytes));

        byte[] getPatternBytes = (byte[]) data.get(GET_PATTERN_KEY);
        sortParameters.setGetPattern(Serializers.deserialize(getPatternBytes, byte[][].class));

        byte[] limitBytes = (byte[]) data.get(LIMIT_KEY);
        sortParameters.setLimit(rangeSerializer.deserialize(limitBytes));

        return sortParameters;
    }

    public static class RangeSerializer extends AbstractSerializer<SortParameters.Range> {

        private static final LongSerializer longSerializer = LongSerializer.INSTANCE;

        public static final RangeSerializer INSTANCE = new RangeSerializer();

        @Override
        protected int calcBytesLength() {
            return longSerializer.getBytesLength() * 2;
        }

        @Override
        protected byte[] doSerialize(SortParameters.Range range) throws SerializationException {
            long start = range.getStart();
            long count = range.getCount();

            byte[] startBytes = longSerializer.serialize(start);
            byte[] countBytes = longSerializer.serialize(count);
            int length = startBytes.length + countBytes.length;
            byte[] bytes = new byte[length];
            int index = 0;
            for (int i = 0; i < startBytes.length; i++) {
                bytes[index++] = startBytes[i];
            }
            for (int i = 0; i < countBytes.length; i++) {
                bytes[index++] = countBytes[i];
            }

            return bytes;
        }

        @Override
        protected SortParameters.Range doDeserialize(byte[] bytes) throws SerializationException {
            int length = bytes.length;
            int size = length / 2;
            byte[] startBytes = new byte[size];
            byte[] countBytes = new byte[size];
            int index = 0;
            for (int i = 0; i < size; i++) {
                startBytes[i] = bytes[index++];
            }
            for (int i = 0; i < size; i++) {
                countBytes[i] = bytes[index++];
            }
            long start = longSerializer.deserialize(startBytes);
            long count = longSerializer.deserialize(countBytes);

            return new SortParameters.Range(start, count);
        }
    }
}
