package io.github.microsphere.spring.redis.serializer;

import org.springframework.data.geo.Point;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * {@link PointSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class PointSerializerTest extends AbstractSerializerTest<Point> {

    @Override
    protected RedisSerializer<Point> getSerializer() {
        return PointSerializer.INSTANCE;
    }

    @Override
    protected Point getValue() {
        return new Point(12.3, 45.6);
    }
}
