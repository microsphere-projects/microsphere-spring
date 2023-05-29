package io.microsphere.spring.redis.metadata;

import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * {@link RedisCommands Redis command} Method parameter meta information
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class ParameterMetadata {

    private final int parameterIndex;

    private final String parameterType;

    private final @Nullable String parameterName;

    public ParameterMetadata(int parameterIndex, String parameterType, @Nullable String parameterName) {
        this.parameterIndex = parameterIndex;
        this.parameterType = parameterType;
        this.parameterName = parameterName;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    public String getParameterType() {
        return parameterType;
    }

    @Nullable
    public String getParameterName() {
        return parameterName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParameterMetadata)) return false;
        ParameterMetadata that = (ParameterMetadata) o;
        return parameterIndex == that.parameterIndex && Objects.equals(parameterType, that.parameterType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterIndex, parameterType);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ParameterMetadata.class.getSimpleName() + "[", "]")
                .add("parameterIndex=" + parameterIndex)
                .add("parameterType='" + parameterType + "'")
                .add("parameterName='" + parameterName + "'")
                .toString();
    }
}
