package io.microsphere.spring.redis.serializer;

import org.junit.Test;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static io.microsphere.spring.redis.serializer.Serializers.defaultSerializer;
import static io.microsphere.spring.redis.serializer.Serializers.getSerializer;
import static io.microsphere.spring.redis.serializer.Serializers.stringSerializer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * {@link Serializers} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class SerializersTest {

    @Test
    public void testTypedSerializers() {
        assertFalse(Serializers.typedSerializers.isEmpty());
    }

    @Test
    public void testGetSerializer() {

        RedisSerializer serializer = getSerializer(Expiration.class);
        assertEquals(ExpirationSerializer.class, serializer.getClass());

        serializer = getSerializer("org.springframework.data.redis.core.types.Expiration");
        assertEquals(ExpirationSerializer.class, serializer.getClass());

        serializer = getSerializer((Class) null);
        assertNull(serializer);
    }

    @Test
    public void testGetSimpleSerializers() {
        // boolean 或 Boolean 类型
        assertEquals(getSerializer(boolean.class), BooleanSerializer.INSTANCE);
        assertEquals(getSerializer(Boolean.class), BooleanSerializer.INSTANCE);

        // int 或 Integer 类型
        assertEquals(getSerializer(int.class), IntegerSerializer.INSTANCE);
        assertEquals(getSerializer(Integer.class), IntegerSerializer.INSTANCE);

        // long 或 Long 类型
        assertEquals(getSerializer(long.class), LongSerializer.INSTANCE);
        assertEquals(getSerializer(Long.class), LongSerializer.INSTANCE);

        // double 或 Double 类型
        assertEquals(getSerializer(double.class), DoubleSerializer.INSTANCE);
        assertEquals(getSerializer(Double.class), DoubleSerializer.INSTANCE);

        // String 类型
        assertEquals(getSerializer(String.class), stringSerializer);
    }

    @Test
    public void testGetArrayTypeSerializers() {
        // byte[] 类型
        assertEquals(getSerializer(byte[][].class), defaultSerializer);

        // int[] 类型
        assertEquals(getSerializer(int[].class), defaultSerializer);

        // byte[][] 类型
        assertEquals(getSerializer(int[].class), defaultSerializer);
    }

    @Test
    public void testGetCollectionTypeSerializers() {
        // Iterable 类型
        assertEquals(getSerializer(Iterable.class), defaultSerializer);

        // Iterator 类型
        assertEquals(getSerializer(Iterator.class), defaultSerializer);

        // Collection 类型
        assertEquals(getSerializer(Collection.class), defaultSerializer);

        // List 类型
        assertEquals(getSerializer(Collection.class), defaultSerializer);

        // Set 类型
        assertEquals(getSerializer(Collection.class), defaultSerializer);

        // Map 类型
        assertEquals(getSerializer(Collection.class), defaultSerializer);

        // Queue 类型
        assertEquals(getSerializer(Collection.class), defaultSerializer);
    }

    @Test
    public void testGetEnumerationSerializers() {
        assertEquals(getSerializer(TimeUnit.class), new EnumSerializer(TimeUnit.class));
    }

    @Test
    public void testGetSpringDataRedisSerializers() {

        // org.springframework.data.redis.core.types.Expiration 类型
        assertEquals(getSerializer(Expiration.class), ExpirationSerializer.INSTANCE);

        // org.springframework.data.redis.connection.SortParameters 类型
        assertEquals(getSerializer(SortParameters.class), SortParametersSerializer.INSTANCE);

        // org.springframework.data.redis.connection.RedisListCommands.Position 类型
        assertEquals(getSerializer(RedisListCommands.Position.class), new EnumSerializer(RedisListCommands.Position.class));

        // org.springframework.data.redis.connection.RedisStringCommands.SetOption 类型
        assertEquals(getSerializer(RedisStringCommands.SetOption.class), new EnumSerializer(RedisStringCommands.SetOption.class));

        // org.springframework.data.redis.connection.RedisZSetCommands.Range 类型
        assertEquals(getSerializer(RedisZSetCommands.Range.class), RangeSerializer.INSTANCE);

        // org.springframework.data.redis.connection.RedisZSetCommands.Aggregate
        assertEquals(getSerializer(RedisZSetCommands.Aggregate.class), new EnumSerializer(RedisZSetCommands.Aggregate.class));

        // org.springframework.data.redis.connection.RedisZSetCommands.Weights 类型
        assertEquals(getSerializer(RedisZSetCommands.Weights.class), WeightsSerializer.INSTANCE);

        // org.springframework.data.redis.connection.ReturnType 类型
        assertEquals(getSerializer(ReturnType.class), new EnumSerializer(ReturnType.class));

        // org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation 类型
        assertEquals(getSerializer(RedisGeoCommands.GeoLocation.class), GeoLocationSerializer.INSTANCE);

        // org.springframework.data.geo.Point 类型
        assertEquals(getSerializer(Point.class), PointSerializer.INSTANCE);
    }
}
