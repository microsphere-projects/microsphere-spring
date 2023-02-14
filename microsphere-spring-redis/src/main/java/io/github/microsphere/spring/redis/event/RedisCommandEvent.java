package io.github.microsphere.spring.redis.event;

import io.github.microsphere.spring.redis.interceptor.RedisMethodContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.redis.connection.RedisCommands;
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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

import static io.github.microsphere.spring.redis.serializer.RedisCommandEventSerializer.VERSION_DEFAULT;
import static io.github.microsphere.spring.redis.serializer.RedisCommandEventSerializer.VERSION_V1;


/**
 * {@link RedisCommands Redis command} event
 * The supported commands：
 * <ul>
 *     <li>RedisStringCommands</li>
 *     <li>RedisHashCommands</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisCommands
 * @see RedisKeyCommands
 * @see RedisStringCommands
 * @see RedisListCommands
 * @see RedisSetCommands
 * @see RedisZSetCommands
 * @see RedisHashCommands
 * @see RedisTxCommands
 * @see RedisPubSubCommands
 * @see RedisConnectionCommands
 * @see RedisServerCommands
 * @see RedisScriptingCommands
 * @see RedisGeoCommands
 * @see RedisHyperLogLogCommands
 * @since 1.0.0
 */
public class RedisCommandEvent extends ApplicationEvent {

    private static final long serialVersionUID = -1L;

    private final transient String applicationName;

    private final transient String sourceBeanName;

    private final transient Method method;

    private final transient Object[] args;

    private transient String interfaceName;

    private transient Class<?>[] parameterTypes;

    private transient int parameterCount = -1;

    private transient byte serializationVersion = VERSION_V1;

    protected RedisCommandEvent(Object source, String applicationName, String sourceBeanName, Method method, Object... args) {
        super(source);
        this.applicationName = applicationName;
        this.sourceBeanName = sourceBeanName;
        this.method = method;
        this.args = args;
    }

    public RedisCommandEvent(@NonNull RedisMethodContext redisMethodContext) {
        this(redisMethodContext, redisMethodContext.getApplicationName(), redisMethodContext.getSourceBeanName(), redisMethodContext.getMethod(), redisMethodContext.getArgs());
    }

    public static class Builder {

        private final Object source;

        private String applicationName;

        private String sourceBeanName;

        private Method method;

        private Object[] args;

        private byte serializationVersion = VERSION_DEFAULT;

        protected Builder(Object source) {
            this.source = source;
        }

        public static Builder source(Object source) {
            return new Builder(source);
        }

        public Builder applicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder sourceBeanName(String sourceBeanName) {
            this.sourceBeanName = sourceBeanName;
            return this;
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder args(Object... args) {
            this.args = args;
            return this;
        }

        public Builder serializationVersion(byte serializationVersion) {
            this.serializationVersion = serializationVersion;
            return this;
        }

        public Method getMethod() {
            return method;
        }

        public RedisCommandEvent build() {
            RedisCommandEvent redisCommandEvent = new RedisCommandEvent(source, applicationName, sourceBeanName, method, args);
            redisCommandEvent.setSerializationVersion(serializationVersion);
            return redisCommandEvent;
        }
    }

    /**
     * Command method
     *
     * @return
     */
    public @NonNull Method getMethod() {
        return method;
    }

    /**
     * @return Command interface name, such as：
     * <ul>
     *     <li>"org.springframework.data.redis.connection.RedisStringCommands"</li>
     *     <li>"org.springframework.data.redis.connection.RedisHashCommands"</li>
     * </ul>
     */
    public String getInterfaceName() {
        String interfaceName = this.interfaceName;
        if (interfaceName == null) {
            interfaceName = resolveInterfaceName(this.method);
            this.interfaceName = interfaceName;
        }
        return interfaceName;
    }

    private String resolveInterfaceName(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        String className = declaringClass.getName();
        return className;
    }

    public String getMethodName() {
        return method.getName();
    }

    public Class<?>[] getParameterTypes() {
        Class<?>[] parameterTypes = this.parameterTypes;
        if (parameterTypes == null) {
            parameterTypes = method.getParameterTypes();
            this.parameterTypes = parameterTypes;
        }
        return parameterTypes;
    }

    public int getParameterCount() {
        int parameterCount = this.parameterCount;
        if (parameterCount == -1) {
            parameterCount = method.getParameterCount();
            this.parameterCount = parameterCount;
        }
        return parameterCount;
    }

    public @Nullable Object[] getArgs() {
        return this.args;
    }

    public @Nullable Object getArg(int index) {
        return this.args[index];
    }

    /**
     * @return Event source Application name
     */
    public @NonNull String getApplicationName() {
        return applicationName;
    }

    /**
     * Source Bean name (non-serialized field, initialized by the consumer)
     *
     * @return Source Bean name
     */
    public @Nullable String getSourceBeanName() {
        return this.sourceBeanName;
    }

    public void setSerializationVersion(byte serializationVersion) {
        this.serializationVersion = serializationVersion;
    }

    public byte getSerializationVersion() {
        return serializationVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedisCommandEvent that = (RedisCommandEvent) o;
        return Objects.equals(applicationName, that.applicationName) &&
                Objects.equals(sourceBeanName, that.sourceBeanName) &&
                Objects.equals(method, that.method) &&
                Arrays.deepEquals(args, that.args);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(applicationName, sourceBeanName, method);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RedisCommandEvent.class.getSimpleName() + "[", "]").add("applicationName='" + applicationName + "'").add("sourceBeanName='" + sourceBeanName + "'").add("method=" + method).add("args=" + Arrays.toString(args)).toString();
    }
}
