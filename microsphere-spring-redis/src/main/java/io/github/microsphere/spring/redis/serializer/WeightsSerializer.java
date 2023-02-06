package io.github.microsphere.spring.redis.serializer;

import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.List;

import static io.github.microsphere.spring.redis.serializer.Serializers.defaultSerialize;

/**
 * {@link RedisZSetCommands.Weights} {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class WeightsSerializer extends AbstractSerializer<RedisZSetCommands.Weights> {

    public static final WeightsSerializer INSTANCE = new WeightsSerializer();

    @Override
    protected byte[] doSerialize(RedisZSetCommands.Weights weights) throws SerializationException {
        List<Double> doubles = weights.toList();
        return defaultSerialize(doubles);
    }

    @Override
    protected RedisZSetCommands.Weights doDeserialize(byte[] bytes) throws SerializationException {
        List<Double> doubles = Serializers.deserialize(bytes, List.class);
        int size = doubles.size();
        double[] weights = new double[size];
        for (int i = 0; i < size; i++) {
            weights[i] = doubles.get(i);
        }
        return RedisZSetCommands.Weights.of(weights);
    }
}
