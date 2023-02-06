package io.github.microsphere.spring.redis.serializer;

import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.HashMap;
import java.util.Map;

import static io.github.microsphere.spring.redis.serializer.Serializers.defaultSerialize;

/**
 * {@link RedisGeoCommands.GeoLocation} {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class GeoLocationSerializer extends AbstractSerializer<RedisGeoCommands.GeoLocation> {

    private static final PointSerializer pointSerializer = PointSerializer.INSTANCE;

    public static final GeoLocationSerializer INSTANCE = new GeoLocationSerializer();

    private static final String NAME_KEY = "n";

    private static final String POINT_KEY = "p";

    @Override
    protected byte[] doSerialize(RedisGeoCommands.GeoLocation geoLocation) throws SerializationException {
        Object name = geoLocation.getName();
        Point point = geoLocation.getPoint();
        Map<String, Object> data = new HashMap<>(2);
        // nameBytes may fail
        byte[] nameBytes = defaultSerialize(name);
        byte[] pointBytes = pointSerializer.serialize(point);

        data.put(NAME_KEY, nameBytes);
        data.put(POINT_KEY, pointBytes);

        return defaultSerialize(data);
    }

    @Override
    protected RedisGeoCommands.GeoLocation doDeserialize(byte[] bytes) throws SerializationException {
        Map<String, Object> data = Serializers.deserialize(bytes, Map.class);

        byte[] nameBytes = (byte[]) data.get(NAME_KEY);
        byte[] pointBytes = (byte[]) data.get(POINT_KEY);

        Object object = Serializers.deserialize(nameBytes, Object.class);
        Point point = pointSerializer.deserialize(pointBytes);

        return new RedisGeoCommands.GeoLocation(object, point);
    }
}
