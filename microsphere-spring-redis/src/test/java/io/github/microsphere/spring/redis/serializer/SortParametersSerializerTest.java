package io.github.microsphere.spring.redis.serializer;

import org.springframework.data.redis.connection.DefaultSortParameters;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.StringJoiner;

/**
 * {@link SortParametersSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class SortParametersSerializerTest extends AbstractSerializerTest<SortParameters> {

    @Override
    protected RedisSerializer<SortParameters> getSerializer() {
        return SortParametersSerializer.INSTANCE;
    }

    @Override
    protected SortParameters getValue() {
        return new DefaultSortParameters("a".getBytes(StandardCharsets.UTF_8),
                new SortParameters.Range(0, 10),
                new byte[0][0],
                SortParameters.Order.ASC,
                true);
    }

    @Override
    protected Object getTestData(SortParameters value) {
        StringJoiner stringJoiner = new StringJoiner(":");
        return stringJoiner
                .add(String.valueOf(value.getOrder()))
                .add(String.valueOf(value.getLimit().getStart()))
                .add(String.valueOf(value.getLimit().getCount()))
                .add(String.valueOf(value.isAlphabetic()))
                .add(Arrays.toString(value.getByPattern()))
                .add(Arrays.deepToString(value.getGetPattern()))
                .toString();
    }
}
