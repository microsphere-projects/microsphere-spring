package io.microsphere.spring.redis.serializer;

import io.microsphere.spring.redis.event.RedisCommandEvent;
import io.microsphere.spring.redis.metadata.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * {@link RedisSerializer} Utilities class, mainly used for Redis command method parameter type
 * serialization and deserialization
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class Serializers {

    private static final Logger logger = LoggerFactory.getLogger(Serializers.class);

    private static final ClassLoader classLoader = Serializers.class.getClassLoader();

    public static final JdkSerializationRedisSerializer defaultSerializer = new JdkSerializationRedisSerializer();

    public static final StringRedisSerializer stringSerializer = new StringRedisSerializer();

    /**
     * Generic parameterized {@link RedisSerializer}
     * Key is the full name of the type, and Value is implemented as {@link RedisSerializer}
     */
    static final Map<String, RedisSerializer<?>> typedSerializers = new HashMap<>(32);

    static {
        initializeBuiltinSerializers();
        initializeParameterizedSerializers();
    }

    public static RedisSerializer<?> getSerializer(Object object) {
        Class<?> type = object.getClass();
        return getSerializer(type);
    }

    public static <T> RedisSerializer<T> getSerializer(Class<?> type) {
        return type == null ? null : (RedisSerializer<T>) getSerializer(type.getName());
    }

    public static RedisSerializer<?> getSerializer(String typeName) {
        RedisSerializer<?> serializer = typedSerializers.get(typeName);

        if (serializer == null) {
            logger.debug("RedisSerializer implementation class of type {} not found, default RedisSerializer implementation class will be used: {}", typeName, defaultSerializer.getClass().getName());
            serializer = defaultSerializer;
            typedSerializers.put(typeName, serializer);
        } else {
            logger.trace("Find the RedisSerializer implementation class of type {} : {}", typeName, serializer.getClass().getName());
        }

        return serializer;
    }

    public static byte[] serializeRawParameter(Parameter parameter) {
        byte[] rawParameterValue = parameter.getRawValue();
        if (rawParameterValue == null) {
            Object parameterValue = parameter.getValue();
            RedisSerializer serializer = getSerializer(parameter.getParameterType());
            if (serializer != null) {
                rawParameterValue = serializer.serialize(parameterValue);
                parameter.setRawValue(rawParameterValue);
            }
        }
        return rawParameterValue;
    }

    public static byte[] defaultSerialize(RedisCommandEvent event) {
        return defaultSerializer.serialize(event);
    }

    public static byte[] defaultSerialize(Object object) {
        return defaultSerializer.serialize(object);
    }

    public static byte[] serialize(Object object) {
        return serialize(object, object.getClass());
    }

    public static byte[] serialize(Object object, Class type) {
        RedisSerializer redisSerializer = getSerializer(type);
        return redisSerializer.serialize(object);
    }

    public static Object deserialize(byte[] bytes, String parameterType) {
        RedisSerializer<?> serializer = getSerializer(parameterType);
        if (serializer == null) {
            logger.error("Unable to deserialize the byte stream to an object of target type {}", parameterType);
            return null;
        }
        Object object = serializer.deserialize(bytes);
        return object;
    }

    public static <T> T deserialize(byte[] bytes, Class<T> type) {
        RedisSerializer<?> serializer = getSerializer(type);
        Object object = serializer.deserialize(bytes);
        if (type.isInstance(object)) {
            return type.cast(object);
        } else {
            logger.error("Unable to deserialize the byte stream to an object of target type {}", type.getName());
            return null;
        }
    }

    /**
     * Initializes built-in Serializers
     */
    private static void initializeBuiltinSerializers() {

        // Initializes the simple type Serializers
        initializeSimpleSerializers();

        // Initializes the array type Serializers
        initializeArrayTypeSerializers();

        // Initializes the collection type Serializers
        initializeCollectionTypeSerializers();

        // Initializes the enumeration type Serializers
        initializeEnumerationSerializers();

        // Initializes the Spring Data Redis type Serializers
        initializeSpringDataRedisSerializers();
    }

    /**
     * Initializes the simple type Serializers
     */
    private static void initializeSimpleSerializers() {

        // boolean or Boolean type 
        register(boolean.class, BooleanSerializer.INSTANCE);
        register(Boolean.class, BooleanSerializer.INSTANCE);

        // int or Integer type 
        register(int.class, IntegerSerializer.INSTANCE);
        register(Integer.class, IntegerSerializer.INSTANCE);

        // long or Long type 
        register(long.class, LongSerializer.INSTANCE);
        register(Long.class, LongSerializer.INSTANCE);

        // double or Double type 
        register(double.class, DoubleSerializer.INSTANCE);
        register(Double.class, DoubleSerializer.INSTANCE);

        // String type 
        register(String.class, stringSerializer);
    }

    /**
     * Initializes collection type Serializers
     */
    private static void initializeCollectionTypeSerializers() {

        // Iterable type 
        register(Iterable.class, defaultSerializer);

        // Iterator type 
        register(Iterator.class, defaultSerializer);

        // Collection type 
        register(Collection.class, defaultSerializer);

        // List type 
        register(List.class, defaultSerializer);

        // Set type 
        register(Set.class, defaultSerializer);

        // Map type 
        register(Map.class, defaultSerializer);

        // Queue type 
        register(Queue.class, defaultSerializer);
    }

    /**
     * Initializes Array type Serializers
     */
    private static void initializeArrayTypeSerializers() {

        // byte[] type 
        register(byte[].class, ByteArraySerializer.INSTANCE);

        // int[] type 
        register(int[].class, defaultSerializer);

        // byte[][] type 
        register(byte[][].class, defaultSerializer);
    }

    /**
     * Initializes Enumeration type Serializers
     */
    private static void initializeEnumerationSerializers() {
        register(TimeUnit.class, new EnumSerializer(TimeUnit.class));
    }

    /**
     * Initializes Spring Data Redis type Serializers
     */
    private static void initializeSpringDataRedisSerializers() {

        // org.springframework.data.redis.core.types.Expiration type 
        register(Expiration.class, ExpirationSerializer.INSTANCE);

        // org.springframework.data.redis.connection.SortParameters type 
        register(SortParameters.class, SortParametersSerializer.INSTANCE);

        // org.springframework.data.redis.connection.RedisListCommands.Position type 
        register(RedisListCommands.Position.class, new EnumSerializer(RedisListCommands.Position.class));

        // org.springframework.data.redis.connection.RedisStringCommands.SetOption type 
        register(RedisStringCommands.SetOption.class, new EnumSerializer(RedisStringCommands.SetOption.class));

        // org.springframework.data.redis.connection.RedisZSetCommands.Range type 
        register(RedisZSetCommands.Range.class, RangeSerializer.INSTANCE);

        // org.springframework.data.redis.connection.RedisZSetCommands.Aggregate
        register(RedisZSetCommands.Aggregate.class, new EnumSerializer(RedisZSetCommands.Aggregate.class));

        // org.springframework.data.redis.connection.RedisZSetCommands.Weights type 
        register(RedisZSetCommands.Weights.class, WeightsSerializer.INSTANCE);

        // org.springframework.data.redis.connection.ReturnType type 
        register(ReturnType.class, new EnumSerializer(ReturnType.class));

        // org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation type 
        register(RedisGeoCommands.GeoLocation.class, GeoLocationSerializer.INSTANCE);

        // org.springframework.data.geo.Point type 
        register(Point.class, PointSerializer.INSTANCE);
    }

    /**
     * Initializes Parameterized Serializers
     */
    private static void initializeParameterizedSerializers() {
        List<RedisSerializer> serializers = loadSerializers();
        initializeParameterizedSerializers(serializers);
    }

    private static void initializeParameterizedSerializers(List<RedisSerializer> serializers) {
        for (RedisSerializer serializer : serializers) {
            initializeParameterizedSerializer(serializer);
        }
    }

    private static void initializeParameterizedSerializer(RedisSerializer serializer) {
        Class<?> parameterizedType = ResolvableType.forType(serializer.getClass()).as(RedisSerializer.class).getGeneric(0).resolve();

        if (parameterizedType != null) {
            register(parameterizedType, serializer);
        } else {
            logger.warn("RedisSerializer implementation class: {} could not find parameter type", serializer.getClass());
        }
    }

    private static List<RedisSerializer> loadSerializers() {
        return SpringFactoriesLoader.loadFactories(RedisSerializer.class, classLoader);
    }

    public static void register(Class<?> type, RedisSerializer<?> serializer) {
        String typeName = type.getName();
        RedisSerializer oldSerializer = typedSerializers.put(typeName, serializer);
        logger.debug("The RedisSerializer[class : '{}' , target type : '{}'] for type['{}'] was registered", getTypeName(serializer), getTypeName(serializer.getTargetType()), getTypeName(type));
        if (oldSerializer != null && !Objects.equals(oldSerializer, serializer)) {
            logger.warn("The RedisSerializer for type['{}'] has been replaced old [class : '{}' , target type : '{}'] -> new [class : '{}' , target type : '{}']",
                    getTypeName(type), getTypeName(oldSerializer), getTypeName(oldSerializer.getTargetType()), getTypeName(serializer), getTypeName(serializer.getTargetType()));
        }
    }

    private static String getTypeName(Object object) {
        return getTypeName(object.getClass());
    }

    private static String getTypeName(Type type) {
        return type.getTypeName();
    }
}
