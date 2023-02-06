package io.github.microsphere.spring.redis.metadata;

import io.github.microsphere.spring.redis.event.RedisCommandEvent;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.connection.RedisHyperLogLogCommands;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.connection.RedisPubSubCommands;
import org.springframework.data.redis.connection.RedisScriptingCommands;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.RedisSetCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisTxCommands;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.util.ReflectionUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.github.microsphere.spring.redis.util.RedisCommandsUtils.buildCommandMethodId;
import static io.github.microsphere.spring.redis.util.RedisCommandsUtils.buildParameterMetadata;
import static io.github.microsphere.spring.redis.util.RedisConstants.FAIL_FAST_ENABLED;
import static io.github.microsphere.spring.redis.util.RedisConstants.FAIL_FAST_ENABLED_PROPERTY_NAME;
import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;
import static org.springframework.util.ClassUtils.forName;
import static org.springframework.util.ReflectionUtils.findMethod;

/**
 * Redis Method Metadata Repository
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@Deprecated
public class MethodMetadataRepository {

    private static final Logger logger = LoggerFactory.getLogger(MethodMetadataRepository.class);

    private static volatile boolean initialized = false;

    /**
     * Interface Class name and {@link Class} object cache (reduces class loading performance cost)
     */
    static final Map<String, Class<?>> redisCommandInterfacesCache = new HashMap<>();

    /**
     * Command interface class name and {@link RedisConnection} command object function
     * (such as: {@link RedisConnection#keyCommands ()}) binding
     */
    static final Map<String, Function<RedisConnection, Object>> redisCommandBindings = new HashMap<>();

    static final Map<Method, List<ParameterMetadata>> writeCommandMethodsMetadata = new HashMap<>();

    /**
     * Method Simple signature with {@link Method} object caching (reduces reflection cost)
     */
    static final Map<String, Method> writeCommandMethodsCache = new HashMap<>();

    static RedisMetadata redisMetadata;

    static {
        init();
    }

    /**
     * Initialize Method Metadata
     */
    public static void init() {
        if (initialized) {
            return;
        }
        initRedisMetadata();
        initRedisMethodsAccessible();
        initRedisCommandsInterfaces();
        initWriteCommandMethods();
        initialized = true;
    }

    private static void initRedisMetadata() {
        redisMetadata = loadRedisMetadata();
    }

    private static RedisMetadata loadRedisMetadata() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        RedisMetadata redisMetadata = new RedisMetadata();
        try {
            Resource[] resources = resolver.getResources(CLASSPATH_ALL_URL_PREFIX + "/META-INF/redis-metadata.yaml");
            for (Resource resource : resources) {
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

    public static boolean isWriteCommandMethod(Method method) {
        return writeCommandMethodsMetadata.containsKey(method);
    }

    public static List<ParameterMetadata> getWriteParameterMetadataList(Method method) {
        return writeCommandMethodsMetadata.get(method);
    }

    public static Method findWriteCommandMethod(RedisCommandEvent event) {
        return event.getMethod();
    }

    public static Method findWriteCommandMethod(String interfaceNme, String methodName, String[] parameterTypes) {
        Method method = getWriteCommandMethod(interfaceNme, methodName, parameterTypes);
        if (method == null) {
            logger.warn("Redis event publishers and consumers have different apis. Please update consumer microsphere-spring-redis artifacts in time!");
            logger.debug("Redis command methods will use Java reflection to find (interface :{}, method name :{}, parameter list :{})...", interfaceNme, methodName, Arrays.toString(parameterTypes));
            Class<?> interfaceClass = getRedisCommandsInterfaceClass(interfaceNme);
            if (interfaceClass == null) {
                logger.warn("The current Redis consumer cannot find Redis command interface: {}. Please confirm whether the spring-data artifacts API is compatible.", interfaceNme);
                return null;
            }
            Class[] parameterClasses = loadParameterClasses(parameterTypes);
            method = findMethod(interfaceClass, methodName, parameterClasses);
            if (method == null) {
                logger.warn("Current Redis consumer Redis command interface (class name: {}) in the method ({}), command method search end!", interfaceNme, buildCommandMethodId(interfaceNme, methodName, parameterTypes));
                return null;
            }
        }
        return method;
    }

    private static Class[] loadParameterClasses(String[] parameterTypes) {
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
    public static Class<?> getRedisCommandsInterfaceClass(String interfaceName) {
        return redisCommandInterfacesCache.get(interfaceName);
    }

    public static Function<RedisConnection, Object> getRedisCommandBindingFunction(String interfaceName) {
        return redisCommandBindings.getOrDefault(interfaceName, redisConnection -> redisConnection);
    }

    private static void initRedisMethodsAccessible() {
        initRedisMethodsAccessible(RedisConnection.class);
        List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(RedisConnection.class);
        for (Class<?> interfaceClass : allInterfaces) {
            initRedisMethodsAccessible(interfaceClass);
        }
    }

    private static void initRedisMethodsAccessible(Class<?> interfaceClass) {
        for (Method method : interfaceClass.getMethods()) {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
        }
    }

    /**
     * Initializes the name of the {@link RedisCommands} command interface with the {@link Class} object cache
     */
    private static void initRedisCommandsInterfaces() {
        initRedisCommandsInterfacesCache();
        initRedisCommandBindings();
    }

    private static void initRedisCommandsInterfacesCache() {
        Class<?>[] interfaces = RedisCommands.class.getInterfaces();
        for (Class<?> interfaceClass : interfaces) {
            redisCommandInterfacesCache.put(interfaceClass.getName(), interfaceClass);
        }
    }

    private static void initRedisCommandBindings() {
        Class<?> redisConnectionClass = RedisConnection.class;
        for (Map.Entry<String, Class<?>> entry : redisCommandInterfacesCache.entrySet()) {
            String interfaceName = entry.getKey();
            Class<?> interfaceClass = entry.getValue();
            ReflectionUtils.doWithMethods(redisConnectionClass, method -> {
                redisCommandBindings.put(interfaceName, redisConnection -> ReflectionUtils.invokeMethod(method, redisConnection));
                logger.debug("Redis command interface {} Bind RedisConnection command object method {}", interfaceName, method);
            }, method -> interfaceClass.equals(method.getReturnType()) && method.getParameterCount() < 1);
        }
    }

    /**
     * Initializes write {@link RedisCommands} command methods, including:
     * <ul>
     *     <li>{@link #initRedisKeyCommandsWriteCommandMethods() RedisKeyCommands write command method}</li>
     *     <li>{@link #initRedisStringCommandsWriteCommandMethods() RedisStringCommands write command method}</li>
     *     <li>{@link #initRedisListCommandsWriteCommandMethods() RedisListCommands write command method}</li>
     *     <li>{@link #initRedisSetCommandsWriteCommandMethods() RedisSetCommands write command method}</li>
     *     <li>{@link #initRedisZSetCommandsWriteCommandMethods() RedisZSetCommands write command method}</li>
     *     <li>{@link #initRedisHashCommandsWriteCommandMethods() RedisHashCommands write command method}</li>
     *     <li>{@link #initRedisScriptingCommandsWriteCommandMethods() RedisScriptingCommands write command method}</li>
     *     <li>{@link #initRedisGeoCommandsWriteCommandMethods() RedisGeoCommands write command method}</li>
     *     <li>{@link #initRedisHyperLogLogCommandsWriteCommandMethods() RedisHyperLogLogCommands write command method}</li>
     * </ol>
     * <p>
     * Not Support：
     * <ul>
     *     <li>{@link RedisTxCommands}</li>
     *     <li>{@link RedisPubSubCommands}</li>
     *     <li>{@link RedisConnectionCommands}</li>
     *     <li>{@link RedisServerCommands}</li>
     * </ul>
     */
    private static void initWriteCommandMethods() {

        loadWriteCommandMethods();

        // Initialize {@link RedisKeyCommands} write command method
        initRedisKeyCommandsWriteCommandMethods();

        // Initialize {@link RedisStringCommands} write command method
        initRedisStringCommandsWriteCommandMethods();

        // Initialize {@link RedisListCommands} write command method
        initRedisListCommandsWriteCommandMethods();

        // Initialize {@link RedisSetCommands} write command method
        initRedisSetCommandsWriteCommandMethods();

        // Initialize {@link RedisZSetCommands} write command method
        initRedisZSetCommandsWriteCommandMethods();

        // Initialize {@link RedisHashCommands} write command method
        initRedisHashCommandsWriteCommandMethods();

        // Initialize {@link RedisScriptingCommands} write command method
        initRedisScriptingCommandsWriteCommandMethods();

        // Initialize {@link RedisGeoCommands} write command method
        initRedisGeoCommandsWriteCommandMethods();

        // Initialize {@link RedisHyperLogLogCommands}
        initRedisHyperLogLogCommandsWriteCommandMethods();

    }

    private static void loadWriteCommandMethods() {
        RedisMetadata redisMetadata = MethodMetadataRepository.redisMetadata;
        for (MethodMetadata methodMetadata : redisMetadata.getMethods()) {
            if (methodMetadata.isWrite()) {
                String interfaceName = methodMetadata.getInterfaceName();
                Class<?> declaredClass = loadClass(interfaceName);
                String methodName = methodMetadata.getMethodName();
                Class[] parameterTypes = loadParameterClasses(methodMetadata.getParameterTypes());
                initWriteCommandMethod(declaredClass, methodName, parameterTypes);
            }
        }
    }

    /**
     * Initialize {@link RedisKeyCommands} write command method
     */
    private static void initRedisKeyCommandsWriteCommandMethods() {

        /**
         * del(byte[]...) Method 
         * @see <a href="https://redis.io/commands/del">Redis Documentation: DEL</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "del", byte[][].class);

        /**
         * unlink(byte[]...) Method 
         * @see <a href="https://redis.io/commands/unlink">Redis Documentation: UNLINK</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "unlink", byte[][].class);

        /**
         * touch(byte[]...) Method 
         * @see <a href="https://redis.io/commands/touch">Redis Documentation: TOUCH</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "touch", byte[][].class);

        /**
         * rename(byte[], byte[]) Method 
         * @see <a href="https://redis.io/commands/rename">Redis Documentation: RENAME</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "rename", byte[].class, byte[].class);

        /**
         * renameNX(byte[], byte[]) Method 
         * @see <a href="https://redis.io/commands/renamenx">Redis Documentation: RENAMENX</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "renameNX", byte[].class, byte[].class);

        /**
         * expire(byte[], long) Method 
         * @see <a href="https://redis.io/commands/expire">Redis Documentation: EXPIRE</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "expire", byte[].class, long.class);

        /**
         * pExpire(byte[], long) Method 
         * @see <a href="https://redis.io/commands/pexpire">Redis Documentation: PEXPIRE</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "pExpire", byte[].class, long.class);

        /**
         * expireAt(byte[], long) Method 
         * @see <a href="https://redis.io/commands/expireat">Redis Documentation: EXPIREAT</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "expireAt", byte[].class, long.class);

        /**
         * pExpireAt(byte[], long) Method 
         * @see <a href="https://redis.io/commands/pexpireat">Redis Documentation: PEXPIREAT</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "pExpireAt", byte[].class, long.class);

        /**
         * persist(byte[]) Method 
         * @see <a href="https://redis.io/commands/persist">Redis Documentation: PERSIST</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "persist", byte[].class);

        /**
         * move(byte[],int) Method 
         * @see <a href="https://redis.io/commands/move">Redis Documentation: MOVE</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "move", byte[].class, int.class);

        /**
         * ttl(byte[], TimeUnit) Method 
         * @see <a href="https://redis.io/commands/ttl">Redis Documentation: TTL</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "ttl", byte[].class, TimeUnit.class);

        /**
         * pTtl(byte[], TimeUnit) Method 
         * @see <a href="https://redis.io/commands/pttl">Redis Documentation: PTTL</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "pTtl", byte[].class, TimeUnit.class);

        /**
         * sort(byte[], SortParameters) Method 
         * @see <a href="https://redis.io/commands/sort">Redis Documentation: SORT</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "sort", byte[].class, SortParameters.class);

        /**
         * sort(byte[], SortParameters,byte[]) Method 
         * @see <a href="https://redis.io/commands/sort">Redis Documentation: SORT</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "sort", byte[].class, SortParameters.class, byte[].class);

        /**
         * restore(byte[], long, byte[]) Method 
         * @see <a href="https://redis.io/commands/restore">Redis Documentation: RESTORE</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "restore", byte[].class, long.class, byte[].class);

        /**
         * restore(byte[], long, byte[], boolean) Method 
         * @see <a href="https://redis.io/commands/restore">Redis Documentation: RESTORE</a>
         */
        initWriteCommandMethod(RedisKeyCommands.class, "restore", byte[].class, long.class, byte[].class, boolean.class);
    }

    /**
     * Initialize {@link RedisStringCommands} write command method
     */
    private static void initRedisStringCommandsWriteCommandMethods() {

        /**
         * set(byte[],byte[]) Method 
         * @see <a href="https://redis.io/commands/set">Redis Documentation: SET</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "set", byte[].class, byte[].class);

        /**
         * set(byte[], byte[], Expiration, SetOption) Method 
         * @see <a href="https://redis.io/commands/set">Redis Documentation: SET</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "set", byte[].class, byte[].class, Expiration.class, RedisStringCommands.SetOption.class);

        /**
         * setNX(byte[],byte[]) Method 
         * @see <a href="https://redis.io/commands/setnx">Redis Documentation: SETNX</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "setNX", byte[].class, byte[].class);

        /**
         * setEx(byte[], long, byte[]) Method 
         * @see <a href="https://redis.io/commands/setex">Redis Documentation: SETEX</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "setEx", byte[].class, long.class, byte[].class);

        /**
         * pSetEx(byte[], long, byte[]) Method 
         * @see <a href="https://redis.io/commands/psetex">Redis Documentation: PSETEX</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "pSetEx", byte[].class, long.class, byte[].class);

        /**
         * mSet(Map<byte[], byte[]>) Method 
         * @see <a href="https://redis.io/commands/mset">Redis Documentation: MSET</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "mSet", Map.class);

        /**
         * mSetNX(Map<byte[], byte[]>) Method 
         * @see <a href="https://redis.io/commands/msetnx">Redis Documentation: MSETNX</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "mSetNX", Map.class);

        /**
         * incr(byte[]) Method 
         * @see <a href="https://redis.io/commands/incr">Redis Documentation: INCR</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "incr", byte[].class);

        /**
         * incrBy(byte[], long) Method 
         * @see <a href="https://redis.io/commands/incrby">Redis Documentation: INCRBY</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "incrBy", byte[].class, long.class);

        /**
         * incrBy(byte[], double) Method 
         * @see <a href="https://redis.io/commands/incrbyfloat">Redis Documentation: INCRBYFLOAT</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "incrBy", byte[].class, double.class);

        /**
         * decr(byte[]) Method 
         * @see <a href="https://redis.io/commands/decr">Redis Documentation: DECR</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "decr", byte[].class);

        /**
         * decrBy(byte[], long) Method 
         * @see <a href="https://redis.io/commands/decrby">Redis Documentation: DECRBY</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "decrBy", byte[].class, long.class);

        /**
         * append(byte[], byte[]) Method 
         * @see <a href="https://redis.io/commands/append">Redis Documentation: APPEND</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "append", byte[].class, byte[].class);

        /**
         * setRange(byte[], byte[], long) Method 
         * @see <a href="https://redis.io/commands/setrange">Redis Documentation: SETRANGE</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "setRange", byte[].class, byte[].class, long.class);

        /**
         * setBit(byte[], long, boolean) Method 
         * @see <a href="https://redis.io/commands/setbit">Redis Documentation: SETBIT</a>
         */
        initWriteCommandMethod(RedisStringCommands.class, "setBit", byte[].class, long.class, boolean.class);
    }


    /**
     * Initialize {@link RedisListCommands} write command method
     */
    private static void initRedisListCommandsWriteCommandMethods() {

        /**
         * rPush(byte[] ,byte[]...) Method 
         * @see <a href="https://redis.io/commands/rpush">Redis Documentation: RPUSH</a>
         */
        initWriteCommandMethod(RedisListCommands.class, "rPush", byte[].class, byte[][].class);

        /**
         * lPush(byte[] ,byte[]...) Method 
         * @see <a href="https://redis.io/commands/lpush">Redis Documentation: LPUSH</a>
         */
        initWriteCommandMethod(RedisListCommands.class, "lPush", byte[].class, byte[][].class);

        /**
         * rPushX(byte[] ,byte[]) Method 
         * @see <a href="https://redis.io/commands/rpushx">Redis Documentation: RPUSHX</a>
         */
        initWriteCommandMethod(RedisListCommands.class, "rPushX", byte[].class, byte[].class);

        /**
         * lPushX(byte[] ,byte[]) Method 
         * @see <a href="https://redis.io/commands/lpushx">Redis Documentation: LPUSHX</a>
         */
        initWriteCommandMethod(RedisListCommands.class, "lPushX", byte[].class, byte[].class);

        /**
         * lTrim(byte[], long, long) Method 
         * @see <a href="https://redis.io/commands/ltrim">Redis Documentation: LTRIM</a>
         */
        initWriteCommandMethod(RedisListCommands.class, "lTrim", byte[].class, long.class, long.class);

        /**
         * lInsert(byte[], Position, byte[], byte[]) Method 
         * @see <a href="https://redis.io/commands/linsert">Redis Documentation: LINSERT</a>
         */
        initWriteCommandMethod(RedisListCommands.class, "lInsert", byte[].class, RedisListCommands.Position.class, byte[].class, byte[].class);

        /**
         * lSet(byte[], long, byte[]) Method 
         * @see <a href="https://redis.io/commands/lset">Redis Documentation: LSET</a>
         */
        initWriteCommandMethod(RedisListCommands.class, "lSet", byte[].class, long.class, byte[].class);

        /**
         * lRem(byte[], long, byte[]) Method 
         * @see <a href="https://redis.io/commands/lrem">Redis Documentation: LREM</a>
         */
        initWriteCommandMethod(RedisListCommands.class, "lRem", byte[].class, long.class, byte[].class);

        /**
         * lPop(byte[]) Method 
         * @see <a href="https://redis.io/commands/lpop">Redis Documentation: LPOP</a>
         */
        initWriteCommandMethod(RedisListCommands.class, "lPop", byte[].class);

        /**
         * rPop(byte[]) Method 
         * @see <a href="https://redis.io/commands/rpop">Redis Documentation: RPOP</a>
         */
        initWriteCommandMethod(RedisListCommands.class, "rPop", byte[].class);

        /**
         * bLPop(int, byte[]...) Method 
         * @see <a href="https://redis.io/commands/blpop">Redis Documentation: BLPOP</a>
         */
        initWriteCommandMethod(RedisListCommands.class, "bLPop", int.class, byte[][].class);

        /**
         * bRPop(int, byte[]...) Method 
         * @see <a href="https://redis.io/commands/brpop">Redis Documentation: BRPOP</a>
         */
        initWriteCommandMethod(RedisListCommands.class, "bRPop", int.class, byte[][].class);

        /**
         * rPopLPush(byte[], byte[]) Method 
         * @see <a href="https://redis.io/commands/rpoplpush">Redis Documentation: RPOPLPUSH</a>
         */
        initWriteCommandMethod(RedisListCommands.class, "rPopLPush", byte[].class, byte[].class);

        /**
         * bRPopLPush(int, byte[], byte[]) Method 
         * @see <a href="https://redis.io/commands/brpoplpush">Redis Documentation: BRPOPLPUSH</a>
         */
        initWriteCommandMethod(RedisListCommands.class, "bRPopLPush", int.class, byte[].class, byte[].class);
    }

    /**
     * Initialize {@link RedisSetCommands} write command method
     */
    private static void initRedisSetCommandsWriteCommandMethods() {

        /**
         * sAdd(byte[], byte[]...) Method 
         * @see <a href="https://redis.io/commands/sadd">Redis Documentation: SADD</a>
         */
        initWriteCommandMethod(RedisSetCommands.class, "sAdd", byte[].class, byte[][].class);

        /**
         * sRem(byte[], byte[]...) Method 
         * @see <a href="https://redis.io/commands/srem">Redis Documentation: SREM</a>
         */
        initWriteCommandMethod(RedisSetCommands.class, "sRem", byte[].class, byte[][].class);

        /**
         * sPop(byte[]) Method 
         * @see <a href="https://redis.io/commands/spop">Redis Documentation: SPOP</a>
         */
        initWriteCommandMethod(RedisSetCommands.class, "sPop", byte[].class);

        /**
         * sPop(byte[], long) Method 
         * @see <a href="https://redis.io/commands/spop">Redis Documentation: SPOP</a>
         */
        initWriteCommandMethod(RedisSetCommands.class, "sPop", byte[].class, long.class);

        /**
         * sMove(byte[], byte[], byte[]) Method 
         * @see <a href="https://redis.io/commands/smove">Redis Documentation: SMOVE</a>
         */
        initWriteCommandMethod(RedisSetCommands.class, "sMove", byte[].class, byte[].class, byte[].class);

        /**
         * sInterStore(byte[], byte[]...) Method 
         * @see <a href="https://redis.io/commands/sinterstore">Redis Documentation: SINTERSTORE</a>
         */
        initWriteCommandMethod(RedisSetCommands.class, "sInterStore", byte[].class, byte[][].class);

        /**
         * sUnionStore(byte[], byte[]...) Method 
         * @see <a href="https://redis.io/commands/sunionstore">Redis Documentation: SUNIONSTORE</a>
         */
        initWriteCommandMethod(RedisSetCommands.class, "sUnionStore", byte[].class, byte[][].class);

        /**
         * sDiffStore(byte[], byte[]...) Method 
         * @see <a href="https://redis.io/commands/sdiffstore">Redis Documentation: SDIFFSTORE</a>
         */
        initWriteCommandMethod(RedisSetCommands.class, "sDiffStore", byte[].class, byte[][].class);
    }

    /**
     * Initialize {@link RedisZSetCommands} write command method
     */
    private static void initRedisZSetCommandsWriteCommandMethods() {

        /**
         * zAdd(byte[], double, byte[]) Method 
         * @see <a href="https://redis.io/commands/zadd">Redis Documentation: ZADD</a>
         */
        initWriteCommandMethod(RedisZSetCommands.class, "zAdd", byte[].class, double.class, byte[].class);

        /**
         * zAdd(byte[], Set<Tuple>) Method 
         * @see <a href="https://redis.io/commands/zadd">Redis Documentation: ZADD</a>
         * TODO Support {@link Tuple}
         */
        initWriteCommandMethod(RedisZSetCommands.class, "zAdd", byte[].class, Set.class);

        /**
         * zRem(byte[], byte[]...) Method 
         * @see <a href="https://redis.io/commands/zrem">Redis Documentation: ZREM</a>
         */
        initWriteCommandMethod(RedisZSetCommands.class, "zRem", byte[].class, byte[][].class);

        /**
         * zIncrBy(byte[], double, byte[]) Method 
         * @see <a href="https://redis.io/commands/zrem">Redis Documentation: ZREM</a>
         */
        initWriteCommandMethod(RedisZSetCommands.class, "zIncrBy", byte[].class, double.class, byte[].class);

        /**
         * zRemRange(byte[], long, long) Method 
         * @see <a href="https://redis.io/commands/zremrangebyrank">Redis Documentation: ZREMRANGEBYRANK</a>
         */
        initWriteCommandMethod(RedisZSetCommands.class, "zRemRange", byte[].class, long.class, long.class);

        /**
         * zRemRangeByScore(byte[], Range) Method 
         * @see <a href="https://redis.io/commands/zremrangebyscore">Redis Documentation: ZREMRANGEBYSCORE</a>
         */
        initWriteCommandMethod(RedisZSetCommands.class, "zRemRangeByScore", byte[].class, RedisZSetCommands.Range.class);

        /**
         * zRemRangeByScore(byte[], double, double) Method 
         * @see <a href="https://redis.io/commands/zremrangebyscore">Redis Documentation: ZREMRANGEBYSCORE</a>
         */
        initWriteCommandMethod(RedisZSetCommands.class, "zRemRangeByScore", byte[].class, double.class, double.class);

        /**
         * zUnionStore(byte[], byte[]...) Method 
         * @see <a href="https://redis.io/commands/zunionstore">Redis Documentation: ZUNIONSTORE</a>
         */
        initWriteCommandMethod(RedisZSetCommands.class, "zUnionStore", byte[].class, byte[][].class);

        /**
         * zUnionStore(byte[], Aggregate, int[], byte[]...) Method 
         * @see <a href="https://redis.io/commands/zunionstore">Redis Documentation: ZUNIONSTORE</a>
         * TODO Support {@link Aggregate}
         */
        initWriteCommandMethod(RedisZSetCommands.class, "zUnionStore", byte[].class, RedisZSetCommands.Aggregate.class, int[].class, byte[][].class);

        /**
         * zInterStore(byte[], byte[]...)*
         * @see <a href="https://redis.io/commands/zinterstore">Redis Documentation: ZINTERSTORE</a>
         */
        initWriteCommandMethod(RedisZSetCommands.class, "zInterStore", byte[].class, byte[][].class);

        /**
         * zInterStore(byte[], Aggregate, int[] weights, byte[]...)
         * @see <a href="https://redis.io/commands/zinterstore">Redis Documentation: ZINTERSTORE</a>
         */
        initWriteCommandMethod(RedisZSetCommands.class, "zInterStore", byte[].class, RedisZSetCommands.Aggregate.class, int[].class, byte[][].class);

        /**
         * zInterStore(byte[], Aggregate, RedisZSetCommands.Weights, byte[]... sets)
         * @see <a href="https://redis.io/commands/zinterstore">Redis Documentation: ZINTERSTORE</a>
         */
        initWriteCommandMethod(RedisZSetCommands.class, "zInterStore", byte[].class, RedisZSetCommands.Aggregate.class, RedisZSetCommands.Weights.class, byte[][].class);

    }

    /**
     * Initialize {@link RedisHashCommands} write command method
     */
    private static void initRedisHashCommandsWriteCommandMethods() {

        /**
         * hSet(byte[], byte[], byte[]) Method 
         * @see <a href="https://redis.io/commands/hset">Redis Documentation: HSET</a>
         */
        initWriteCommandMethod(RedisHashCommands.class, "hSet", byte[].class, byte[].class, byte[].class);

        /**
         * hSetNX(byte[], byte[], byte[]) Method 
         * @see <a href="https://redis.io/commands/hsetnx">Redis Documentation: HSETNX</a>
         */
        initWriteCommandMethod(RedisHashCommands.class, "hSetNX", byte[].class, byte[].class, byte[].class);

        /**
         * hMSet(byte[], Map<byte[], byte[]>) Method 
         * @see <a href="https://redis.io/commands/hmset">Redis Documentation: HMSET</a>
         */
        initWriteCommandMethod(RedisHashCommands.class, "hMSet", byte[].class, Map.class);

        /**
         * hIncrBy(byte[], byte[], long) Method 
         * @see <a href="https://redis.io/commands/hmset">Redis Documentation: HMSET</a>
         */
        initWriteCommandMethod(RedisHashCommands.class, "hIncrBy", byte[].class, byte[].class, long.class);

        /**
         * hIncrBy(byte[], byte[], double) Method 
         * @see <a href="https://redis.io/commands/hincrbyfloat">Redis Documentation: HINCRBYFLOAT</a>
         */
        initWriteCommandMethod(RedisHashCommands.class, "hIncrBy", byte[].class, byte[].class, double.class);

        /**
         * hDel(byte[], byte[]...) Method 
         * @see <a href="https://redis.io/commands/hdel">Redis Documentation: HDEL</a>
         */
        initWriteCommandMethod(RedisHashCommands.class, "hDel", byte[].class, byte[][].class);

    }

    /**
     * Initialize {@link RedisScriptingCommands} write command method
     */
    private static void initRedisScriptingCommandsWriteCommandMethods() {

        /**
         * scriptLoad(byte[]) Method 
         * @see <a href="https://redis.io/commands/script-load">Redis Documentation: SCRIPT LOAD</a>
         */
        initWriteCommandMethod(RedisScriptingCommands.class, "scriptLoad", byte[].class);

        /**
         * eval(byte[], ReturnType, int, byte[]...) Method 
         * @see <a href="https://redis.io/commands/eval">Redis Documentation: EVAL</a>
         */
        initWriteCommandMethod(RedisScriptingCommands.class, "eval", byte[].class, ReturnType.class, int.class, byte[][].class);

        /**
         * evalSha(String, ReturnType, int numKeys, byte[]...) Method 
         * @see <a href="https://redis.io/commands/evalsha">Redis Documentation: EVALSHA</a>
         */
        initWriteCommandMethod(RedisScriptingCommands.class, "evalSha", String.class, ReturnType.class, int.class, byte[][].class);

        /**
         * evalSha(byte[], ReturnType, int, byte[]...) Method 
         * @see <a href="https://redis.io/commands/evalsha">Redis Documentation: EVALSHA</a>
         */
        initWriteCommandMethod(RedisScriptingCommands.class, "evalSha", byte[].class, ReturnType.class, int.class, byte[][].class);

    }

    /**
     * Initialize {@link RedisGeoCommands} write command method
     */
    private static void initRedisGeoCommandsWriteCommandMethods() {

        /**
         * geoAdd(byte[], Point, byte[]) Method 
         * @see <a href="https://redis.io/commands/geoadd">Redis Documentation: GEOADD</a>
         */
        initWriteCommandMethod(RedisGeoCommands.class, "geoAdd", byte[].class, Point.class, byte[].class);

        /**
         * geoAdd(byte[], GeoLocation<byte[]>) Method 
         * @see <a href="https://redis.io/commands/geoadd">Redis Documentation: GEOADD</a>
         */
        initWriteCommandMethod(RedisGeoCommands.class, "geoAdd", byte[].class, RedisGeoCommands.GeoLocation.class);

        /**
         * geoAdd(byte[], Map<byte[], Point>) Method 
         * @see <a href="https://redis.io/commands/geoadd">Redis Documentation: GEOADD</a>
         */
        initWriteCommandMethod(RedisGeoCommands.class, "geoAdd", byte[].class, Map.class);

        /**
         * geoAdd(byte[], Iterable<RedisGeoCommands.GeoLocation<byte[]>>) Method 
         * @see <a href="https://redis.io/commands/geoadd">Redis Documentation: GEOADD</a>
         */
        initWriteCommandMethod(RedisGeoCommands.class, "geoAdd", byte[].class, Iterable.class);

        /**
         * geoRemove(byte[], byte[]...) Method 
         * @see <a href="https://redis.io/commands/zrem">Redis Documentation: ZREM</a>
         */
        initWriteCommandMethod(RedisGeoCommands.class, "geoRemove", byte[].class, byte[][].class);
    }

    /**
     * Initialize {@link RedisHyperLogLogCommands}
     */
    private static void initRedisHyperLogLogCommandsWriteCommandMethods() {

        /**
         * pfAdd(byte[], byte[]...) Method 
         * @see <a href="https://redis.io/commands/pfadd">Redis Documentation: PFADD</a>
         */
        initWriteCommandMethod(RedisHyperLogLogCommands.class, "pfAdd", byte[].class, byte[][].class);

        /**
         * pfMerge(byte[], byte[]...) Method 
         * @see <a href="https://redis.io/commands/pfmerge">Redis Documentation: PFMERGE</a>
         */
        initWriteCommandMethod(RedisHyperLogLogCommands.class, "pfMerge", byte[].class, byte[][].class);
    }

    private static void initWriteCommandMethod(Class<?> declaredClass, String methodName, Class<?>... parameterTypes) {
        try {
            logger.debug("Initializes the write command method[Declared Class: {} , Method: {}, Parameter types: {}]...", declaredClass.getName(), methodName, Arrays.toString(parameterTypes));
            Method method = findMethod(declaredClass, methodName, parameterTypes);
            // Reduced Method runtime checks
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            if (initWriteCommandMethodMethod(method, parameterTypes)) {
                initWriteCommandMethodCache(declaredClass, method, parameterTypes);
            }
        } catch (Throwable e) {
            logger.error("Unable to initialize write command method[Declared Class: {}, Method: {}, Parameter types: {}], Reason: {}", declaredClass.getName(), methodName, Arrays.toString(parameterTypes), e.getMessage());
            if (FAIL_FAST_ENABLED) {
                logger.error("Fail-Fast mode is activated and an exception is about to be thrown. You can disable Fail-Fast mode with the JVM startup parameter -D{}=false", FAIL_FAST_ENABLED_PROPERTY_NAME);
                throw new IllegalArgumentException(e);
            }
        }
    }

    private static boolean initWriteCommandMethodMethod(Method method, Class<?>[] parameterTypes) {
        if (writeCommandMethodsMetadata.containsKey(method)) {
            return false;
        }
        List<ParameterMetadata> parameterMetadataList = buildParameterMetadata(method, parameterTypes);
        writeCommandMethodsMetadata.put(method, parameterMetadataList);
        logger.debug("Initializing write command method metadata information successfully, Method: {}, parameter Write Command Method metadata information: {}", method.getName(), parameterMetadataList);
        return true;
    }

    private static void initWriteCommandMethodCache(Class<?> declaredClass, Method method, Class<?>[] parameterTypes) {
        String id = buildCommandMethodId(declaredClass.getName(), method.getName(), parameterTypes);
        if (writeCommandMethodsCache.putIfAbsent(id, method) == null) {
            logger.debug("Cache write command method[id: {}, Method: {}]", id, method);
        } else {
            logger.warn("write command method[id: {}, Method: {}] is cached", id, method);
        }
    }

}
