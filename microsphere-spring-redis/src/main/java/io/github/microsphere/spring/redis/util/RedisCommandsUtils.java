package io.github.microsphere.spring.redis.util;

import io.github.microsphere.spring.redis.event.RedisCommandEvent;
import io.github.microsphere.spring.redis.metadata.Parameter;
import io.github.microsphere.spring.redis.metadata.ParameterMetadata;
import io.github.microsphere.spring.redis.serializer.Serializers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisConnection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

import static io.github.microsphere.spring.redis.metadata.RedisMetadataRepository.getWriteParameterMetadataList;
import static java.util.Collections.unmodifiableList;
import static org.springframework.util.ClassUtils.forName;

/**
 * {@link RedisCommands Redis Command} Utilities Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class RedisCommandsUtils {

    private static final Logger logger = LoggerFactory.getLogger(RedisCommandsUtils.class);

    private static final String REDIS_COMMANDS_PACKAGE_NAME = "org.springframework.data.redis.connection.";

    private static final int REDIS_COMMANDS_PACKAGE_NAME_LENGTH = REDIS_COMMANDS_PACKAGE_NAME.length();

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public static final String REDIS_GEO_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisGeoCommands";

    public static final String REDIS_HASH_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisHashCommands";

    public static final String REDIS_HYPER_LOG_LOG_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisHyperLogLogCommands";

    public static final String REDIS_KEY_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisKeyCommands";

    public static final String REDIS_LIST_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisListCommands";

    public static final String REDIS_SET_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisSetCommands";

    public static final String REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisScriptingCommands";

    public static final String REDIS_SERVER_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisServerCommands";

    public static final String REDIS_STREAM_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisStreamCommands";

    public static final String REDIS_STRING_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisStringCommands";

    public static final String REDIS_ZSET_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisZSetCommands";

    public static final String REDIS_TX_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisTxCommands";

    public static final String REDIS_PUB_SUB_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisPubSubCommands";

    public static final String REDIS_CONNECTION_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisConnectionCommands";

    public static String resolveSimpleInterfaceName(String interfaceName) {
        int index = interfaceName.indexOf(REDIS_COMMANDS_PACKAGE_NAME);
        if (index == 0) {
            return interfaceName.substring(REDIS_COMMANDS_PACKAGE_NAME_LENGTH);
        } else {
            return interfaceName;
        }
    }

    public static String resolveInterfaceName(String interfaceName) {
        int index = interfaceName.indexOf('.');
        if (index == -1) {
            return REDIS_COMMANDS_PACKAGE_NAME + interfaceName;
        } else {
            return interfaceName;
        }
    }

    public static Object getRedisCommands(RedisConnection redisConnection, String interfaceName) {
        switch (interfaceName) {
            case REDIS_STRING_COMMANDS_INTERFACE_NAME:
                return redisConnection.stringCommands();
            case REDIS_HASH_COMMANDS_INTERFACE_NAME:
                return redisConnection.hashCommands();
            case REDIS_LIST_COMMANDS_INTERFACE_NAME:
                return redisConnection.listCommands();
            case REDIS_SET_COMMANDS_INTERFACE_NAME:
                return redisConnection.setCommands();
            case REDIS_ZSET_COMMANDS_INTERFACE_NAME:
                return redisConnection.zSetCommands();
            case REDIS_KEY_COMMANDS_INTERFACE_NAME:
                return redisConnection.keyCommands();
            case REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME:
                return redisConnection.scriptingCommands();

            case REDIS_GEO_COMMANDS_INTERFACE_NAME:
                return redisConnection.geoCommands();
            case REDIS_SERVER_COMMANDS_INTERFACE_NAME:
                return redisConnection.serverCommands();
            case REDIS_STREAM_COMMANDS_INTERFACE_NAME:
                // TODO The Redis Spring Data version needs to be upgraded
                // return redisConnection.streamCommands();
            default:
                throw new UnsupportedOperationException(interfaceName);
        }

    }

    public static String buildCommandMethodId(RedisCommandEvent event) {
        return buildCommandMethodId(event.getMethod());
    }

    public static String buildCommandMethodId(Method redisCommandMethod) {
        String interfaceName = redisCommandMethod.getDeclaringClass().getName();
        String methodName = redisCommandMethod.getName();
        Class<?>[] parameterTypes = redisCommandMethod.getParameterTypes();
        return buildCommandMethodId(interfaceName, methodName, parameterTypes);
    }

    public static String buildCommandMethodId(String interfaceName, String methodName, Class<?>... parameterTypes) {
        int length = parameterTypes.length;
        String[] parameterTypeNames = new String[length];
        for (int i = 0; i < length; i++) {
            parameterTypeNames[i] = parameterTypes[i].getName();
        }
        return buildCommandMethodId(interfaceName, methodName, parameterTypeNames);
    }

    public static String buildCommandMethodId(String interfaceName, String methodName, String... parameterTypes) {
        StringBuilder infoBuilder = new StringBuilder(interfaceName);
        infoBuilder.append(".").append(methodName);
        StringJoiner paramTypesInfo = new StringJoiner(",", "(", ")");
        for (String parameterType : parameterTypes) {
            paramTypesInfo.add(parameterType);
        }
        infoBuilder.append(paramTypesInfo);
        return infoBuilder.toString();
    }

    /**
     * @param method         the Redis command {@link Method}
     * @param args           the parameter values of the Redis command {@link Method}
     * @param consumer       The one {@link BiConsumer BiConsumer} of {@link Parameter} and its index
     * @param otherConsumers The others {@link BiConsumer BiConsumers} of {@link Parameter} and its index
     * @return if the parameters from the write method, return <code>true</code>, or <code>false</code>
     */
    public static boolean initParameters(Method method, Object[] args, BiConsumer<Parameter, Integer> consumer, BiConsumer<Parameter, Integer>... otherConsumers) {

        boolean sourceFromWriteMethod = true;

        List<ParameterMetadata> parameterMetadataList = null;

        try {
            // First, attempt to get the cached list of ParameterMetadata from the write method
            parameterMetadataList = getWriteParameterMetadataList(method);
            // If not found, try to build them
            if (parameterMetadataList == null) {
                sourceFromWriteMethod = false;
                parameterMetadataList = buildParameterMetadataList(method);
            }

            int size = parameterMetadataList.size();
            int otherConsumerCount = otherConsumers.length;

            if (size > 0) {

                for (int i = 0; i < size; i++) {
                    Object parameterValue = args[i];
                    ParameterMetadata parameterMetadata = parameterMetadataList.get(i);
                    Parameter parameter = new Parameter(parameterValue, parameterMetadata);
                    // serialize parameter
                    Serializers.serializeRawParameter(parameter);
                    // consumer one
                    consumer.accept(parameter, i);
                    // consumer others
                    for (int j = 0; j < otherConsumerCount; j++) {
                        BiConsumer<Parameter, Integer> parameterConsumer = otherConsumers[j];
                        parameterConsumer.accept(parameter, i);
                    }
                }
            }
        } catch (Throwable e) {
            logger.error("Redis failed to initialize Redis command method parameter {}!", parameterMetadataList, e);
        }

        return sourceFromWriteMethod;
    }

    public static List<ParameterMetadata> buildParameterMetadataList(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return buildParameterMetadata(method, parameterTypes);
    }

    public static List<ParameterMetadata> buildParameterMetadata(Method method, Class<?>[] parameterTypes) {
        int parameterCount = parameterTypes.length;
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        List<ParameterMetadata> parameterMetadataList = new ArrayList<>(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            String parameterType = parameterTypes[i].getName();
            String parameterName = parameterNames[i];
            ParameterMetadata parameterMetadata = new ParameterMetadata(i, parameterType, parameterName);
            parameterMetadataList.add(parameterMetadata);
            // Preload the RedisSerializer implementation for the Method parameter type
            Serializers.getSerializer(parameterType);
        }
        return unmodifiableList(parameterMetadataList);
    }

    public static Class[] loadParameterClasses(String... parameterTypes) {
        int parameterCount = parameterTypes.length;
        Class[] parameterClasses = new Class[parameterCount];
        for (int i = 0; i < parameterCount; i++) {
            String parameterType = parameterTypes[i];
            Class parameterClass = loadClass(parameterType);
            parameterClasses[i] = parameterClass;
        }
        return parameterClasses;
    }

    private static Class loadClass(String className) {
        Class type = null;
        try {
            type = forName(className, null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return type;
    }

}
