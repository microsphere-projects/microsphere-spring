package io.microsphere.spring.redis.serializer;

import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.HashMap;
import java.util.Map;

import static io.microsphere.spring.redis.serializer.Serializers.defaultSerialize;


/**
 * {@link RedisZSetCommands.Range} {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RangeSerializer extends AbstractSerializer<RedisZSetCommands.Range> {

    public static final RangeSerializer INSTANCE = new RangeSerializer();

    private static final String MAX_VALUE_KEY = "xv";

    private static final String MAX_INCLUDING_KEY = "xi";

    private static final String MIN_VALUE_KEY = "mv";

    private static final String MIN_INCLUDING_KEY = "mi";

    @Override
    protected byte[] doSerialize(RedisZSetCommands.Range range) throws SerializationException {
        Map<String, Object> data = new HashMap<>(4);

        RedisZSetCommands.Range.Boundary max = range.getMax();
        Object maxValue = max == null ? null : max.getValue();
        boolean maxIncluding = max == null ? false : max.isIncluding();

        data.put(MAX_VALUE_KEY, maxValue);
        data.put(MAX_INCLUDING_KEY, maxIncluding);

        RedisZSetCommands.Range.Boundary min = range.getMin();
        Object minValue = min == null ? null : min.getValue();
        boolean minIncluding = min == null ? false : min.isIncluding();

        data.put(MIN_VALUE_KEY, minValue);
        data.put(MIN_INCLUDING_KEY, minIncluding);

        return defaultSerialize(data);
    }

    @Override
    protected RedisZSetCommands.Range doDeserialize(byte[] bytes) throws SerializationException {
        Map<String, Object> data = Serializers.deserialize(bytes, Map.class);

        RedisZSetCommands.Range range = RedisZSetCommands.Range.range();

        Object maxValue = data.get(MAX_VALUE_KEY);
        boolean maxIncluding = (Boolean) data.get(MAX_INCLUDING_KEY);

        if (maxValue != null) {
            range = maxIncluding ? range.lte(maxValue) : range.lt(maxValue);
        }

        Object minValue = data.get(MIN_VALUE_KEY);
        boolean minIncluding = (Boolean) data.get(MIN_INCLUDING_KEY);

        if (minValue != null) {
            range = minIncluding ? range.gte(minValue) : range.gt(minValue);
        }

        if (maxValue == null && minValue == null && maxIncluding && minIncluding) {
            range = RedisZSetCommands.Range.unbounded();
        }

        return range;
    }
}
