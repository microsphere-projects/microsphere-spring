package io.github.microsphere.spring.redis.metadata;

import io.github.microsphere.spring.redis.event.RedisCommandEvent;
import io.github.microsphere.spring.redis.util.RedisCommandsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.util.ReflectionUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static io.github.microsphere.spring.redis.util.RedisCommandsUtils.buildCommandMethodId;
import static io.github.microsphere.spring.redis.util.RedisCommandsUtils.buildParameterMetadata;
import static io.github.microsphere.spring.redis.util.RedisCommandsUtils.loadParameterClasses;
import static io.github.microsphere.spring.redis.util.RedisConstants.FAIL_FAST_ENABLED;
import static io.github.microsphere.spring.redis.util.RedisConstants.FAIL_FAST_ENABLED_PROPERTY_NAME;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;
import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;
import static org.springframework.util.ReflectionUtils.findMethod;

/**
 * Redis Metadata Repository
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RedisMetadataRepository {

    private static final Logger logger = LoggerFactory.getLogger(RedisMetadataRepository.class);


    /**
     * Interface Class name and {@link Class} object cache (reduces class loading performance cost)
     */
    static final Map<String, Class<?>> redisCommandInterfacesCache = initRedisCommandInterfacesCache();

    /**
     * Redis Command {@link Method methods} cache using {@link RedisCommandsUtils#buildCommandMethodId(Method) Method ID} as key
     */
    static final Map<String, Method> redisCommandMethodsCache = initRedisCommandMethodsCache();

    /**
     * Command interface class name and {@link RedisConnection} command object function
     * (such as: {@link RedisConnection#keyCommands()}) binding
     */
    static final Map<String, Function<RedisConnection, Object>> redisCommandBindings = initRedisCommandBindings();

    static final Map<Method, List<ParameterMetadata>> writeCommandMethodsMetadata = new HashMap<>(256);

    /**
     * Method Simple signature with {@link Method} object caching (reduces reflection cost)
     */
    static final Map<String, Method> writeCommandMethodsCache = new HashMap<>(256);

    /**
     * MethodMetadata cache
     * <ul>
     *     <li>If the {@link MethodMetadata#getIndex() Method ID} is a key, the value is {@link Method}.</li>
     *     <li>If the {@link Method} is a key, the value is {@link MethodMetadata}.</li>
     * </ul>
     */
    static final Map<Object, Object> methodMetadataCache = initMethodMetadataCache();

    /**
     * Caches the name of the {@link RedisCommands} command interface with the {@link Class} object cache
     */
    private static Map<String, Class<?>> initRedisCommandInterfacesCache() {
        List<Class<?>> redisCommandInterfaceClasses = getAllInterfaces(RedisCommands.class);
        int size = redisCommandInterfaceClasses.size();
        Map<String, Class<?>> redisCommandInterfacesCache = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            Class<?> redisCommandInterfaceClass = redisCommandInterfaceClasses.get(i);
            String interfaceName = redisCommandInterfaceClass.getName();
            redisCommandInterfacesCache.put(interfaceName, redisCommandInterfaceClass);
            logger.debug("Caches the Redis Command Interface : {}", interfaceName);
        }
        return unmodifiableMap(redisCommandInterfacesCache);
    }

    private static Map<String, Method> initRedisCommandMethodsCache() {
        Collection<Class<?>> redisCommandInterfaceClasses = redisCommandInterfacesCache.values();
        Map<String, Method> redisCommandMethodsCache = new HashMap<>(512);
        for (Class<?> redisCommandInterfaceClass : redisCommandInterfaceClasses) {
            Method[] methods = redisCommandInterfaceClass.getMethods();
            for (Method method : methods) {
                String methodId = buildCommandMethodId(method);
                redisCommandMethodsCache.put(methodId, method);
                setAccessible(method);
                logger.debug("Caches the Redis Command Method : {}", methodId);
            }
        }
        return unmodifiableMap(redisCommandMethodsCache);
    }

    private static Map<String, Function<RedisConnection, Object>> initRedisCommandBindings() {
        Class<?> redisCommandInterfaceClass = RedisConnection.class;
        Method[] redisCommandMethods = redisCommandInterfaceClass.getMethods();
        int length = redisCommandMethods.length;
        Map<String, Function<RedisConnection, Object>> redisCommandBindings = new HashMap<>(1);
        for (int i = 0; i < length; i++) {
            Method redisCommandMethod = redisCommandMethods[i];
            initRedisCommandBindings(redisCommandInterfaceClass, redisCommandMethod, redisCommandBindings);
        }
        return unmodifiableMap(redisCommandBindings);
    }

    private static Map<Object, Object> initMethodMetadataCache() {
        RedisMetadata redisMetadata = loadRedisMetadata();
        List<MethodMetadata> methodMetadataList = redisMetadata.getMethods();
        int size = methodMetadataList.size();
        Map<Object, Object> redisMetadataCache = new HashMap<>(size * 2);
        for (int i = 0; i < size; i++) {
            MethodMetadata methodMetadata = methodMetadataList.get(i);
            Method redisCommandMethod = getRedisCommandMethod(methodMetadata);

            if (redisCommandMethod == null) {
                logger.warn("The Redis Command Method[{}] can't be found in the artifact 'org.springframework.data:spring-data-redis'", methodMetadata);
                continue;
            }

            short id = methodMetadata.getIndex();
            // Put id and Method as key
            if (redisMetadataCache.put(id, redisCommandMethod) == null && redisMetadataCache.put(redisCommandMethod, methodMetadata) == null) {
                if (methodMetadata.isWrite()) {
                    initWriteCommandMethod(redisCommandMethod);
                }
            } else {
                throw new IllegalStateException("Duplicated Redis Command Method was found, " + methodMetadata);
            }
        }
        return redisMetadataCache;
    }

    private static Method getRedisCommandMethod(MethodMetadata methodMetadata) {
        String interfaceName = methodMetadata.getInterfaceName();
        String methodName = methodMetadata.getMethodName();
        String[] parameterTypes = methodMetadata.getParameterTypes();
        return getRedisCommandMethod(interfaceName, methodName, parameterTypes);
    }

    private static RedisMetadata loadRedisMetadata() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        RedisMetadata redisMetadata = new RedisMetadata();
        try {
            Resource[] resources = resolver.getResources(CLASSPATH_ALL_URL_PREFIX + "/META-INF/redis-metadata.yaml");
            int size = resources.length;
            for (int i = 0; i < size; i++) {
                Resource resource = resources[i];
                redisMetadata.merge(loadRedisMetadata(resource));
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return redisMetadata;
    }

    private static RedisMetadata loadRedisMetadata(Resource resource) throws IOException {
        Yaml yaml = new Yaml();
        RedisMetadata redisMetadata = yaml.loadAs(resource.getInputStream(), RedisMetadata.class);
        return redisMetadata;
    }

    public static Short findMethodIndex(Method redisCommandMethod) {
        MethodMetadata methodMetadata = (MethodMetadata) methodMetadataCache.get(redisCommandMethod);
        return methodMetadata == null ? null : methodMetadata.getIndex();
    }

    public static Method findRedisCommandMethod(short methodIndex) {
        Method redisCommandMethod = (Method) methodMetadataCache.get(methodIndex);
        return redisCommandMethod;
    }

    public static boolean isWriteCommandMethod(Method method) {
        return writeCommandMethodsMetadata.containsKey(method);
    }

    public static List<ParameterMetadata> getWriteParameterMetadataList(Method method) {
        return writeCommandMethodsMetadata.get(method);
    }

    public static Method findWriteCommandMethod(RedisCommandEvent event) {
        return event.getMethod();
    }

    public static Method findWriteCommandMethod(String interfaceNme, String methodName, String... parameterTypes) {
        Method method = getWriteCommandMethod(interfaceNme, methodName, parameterTypes);
        if (method == null) {
            logger.warn("Redis event publishers and consumers have different apis. Please update consumer microsphere-spring-redis artifacts in time!");
            logger.debug("Redis command methods will use Java reflection to find (interface :{}, method name :{}, parameter list :{})...", interfaceNme, methodName, Arrays.toString(parameterTypes));
            Class<?> redisCommandInterfaceClass = getRedisCommandInterfaceClass(interfaceNme);
            if (redisCommandInterfaceClass == null) {
                logger.warn("The current Redis consumer cannot find Redis command interface: {}. Please confirm whether the spring-data artifacts API is compatible.", interfaceNme);
                return null;
            }
            Class[] parameterClasses = loadParameterClasses(parameterTypes);
            method = findMethod(redisCommandInterfaceClass, methodName, parameterClasses);
            if (method == null) {
                logger.warn("Current Redis consumer Redis command interface (class name: {}) in the method ({}), command method search end!", interfaceNme, buildCommandMethodId(interfaceNme, methodName, parameterTypes));
                return null;
            }
        }
        return method;
    }


    public static Method getWriteCommandMethod(String interfaceName, String methodName, String... parameterTypes) {
        String id = buildCommandMethodId(interfaceName, methodName, parameterTypes);
        return writeCommandMethodsCache.get(id);
    }

    public static Set<Method> getWriteCommandMethods() {
        return writeCommandMethodsMetadata.keySet();
    }

    /**
     * Gets the {@link RedisCommands} command interface for the specified Class name {@link Class}
     *
     * @param interfaceName {@link RedisCommands} Command interface class name
     * @return If not found, return <code>null<code>
     */
    public static Class<?> getRedisCommandInterfaceClass(String interfaceName) {
        return redisCommandInterfacesCache.get(interfaceName);
    }

    public static Function<RedisConnection, Object> getRedisCommandBindingFunction(String interfaceName) {
        return redisCommandBindings.getOrDefault(interfaceName, redisConnection -> redisConnection);
    }

    public static Method getRedisCommandMethod(String interfaceName, String methodName, String... parameterTypes) {
        String methodId = buildCommandMethodId(interfaceName, methodName, parameterTypes);
        return redisCommandMethodsCache.get(methodId);
    }

    private static void setAccessible(AccessibleObject accessible) {
        if (!accessible.isAccessible()) {
            accessible.setAccessible(true);
        }
    }

    private static void initRedisCommandBindings(Class<?> redisCommandInterfaceClass, Method redisCommandMethod, Map<String, Function<RedisConnection, Object>> redisCommandBindings) {
        Class<?> returnType = redisCommandMethod.getReturnType();
        if (redisCommandInterfaceClass.equals(returnType) && redisCommandMethod.getParameterCount() < 1) {
            String interfaceName = redisCommandInterfaceClass.getName();
            redisCommandBindings.put(interfaceName, redisConnection -> ReflectionUtils.invokeMethod(redisCommandMethod, redisConnection));
            logger.debug("Redis command interface {} Bind RedisConnection command object method {}", interfaceName, redisCommandMethod);
        }
    }

    private static void initWriteCommandMethod(Method method) {
        try {
            Class<?>[] parameterTypes = method.getParameterTypes();
            // Reduced Method runtime checks
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            if (initWriteCommandMethodParameterMetadata(method, parameterTypes)) {
                initWriteCommandMethodCache(method, parameterTypes);
            }
        } catch (Throwable e) {
            logger.error("Unable to initialize write command method[{}], Reason: {}", method, e.getMessage());
            if (FAIL_FAST_ENABLED) {
                logger.error("Fail-Fast mode is activated and an exception is about to be thrown. You can disable Fail-Fast mode with the JVM startup parameter -D{}=false", FAIL_FAST_ENABLED_PROPERTY_NAME);
                throw new IllegalArgumentException(e);
            }
        }
    }

    private static boolean initWriteCommandMethodParameterMetadata(Method method, Class<?>[] parameterTypes) {
        if (writeCommandMethodsMetadata.containsKey(method)) {
            return false;
        }
        List<ParameterMetadata> parameterMetadataList = buildParameterMetadata(method, parameterTypes);
        writeCommandMethodsMetadata.put(method, parameterMetadataList);
        logger.debug("Caches the Redis Write Command Method[{}] Parameter Metadata : {}", method, parameterMetadataList);
        return true;
    }

    private static void initWriteCommandMethodCache(Method method, Class<?>[] parameterTypes) {
        Class<?> declaredClass = method.getDeclaringClass();
        String id = buildCommandMethodId(declaredClass.getName(), method.getName(), parameterTypes);
        if (writeCommandMethodsCache.putIfAbsent(id, method) == null) {
            logger.debug("Caches the Redis Write Command Method : {}", id);
        } else {
            logger.warn("The Redis Write Command Method[{}] was cached", id, method);
        }
    }

}
