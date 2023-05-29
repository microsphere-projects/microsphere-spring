package io.microsphere.spring.redis.metadata;

import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * {@link RedisCommands Redis Command} Method parameters encapsulate the object
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class Parameter {

    private final Object value;

    private final ParameterMetadata metadata;

    private @Nullable byte[] rawValue;

    public Parameter(Object value, ParameterMetadata metadata) {
        this.value = value;
        this.metadata = metadata;
    }

    public Object getValue() {
        return value;
    }

    public ParameterMetadata getMetadata() {
        return metadata;
    }

    public @Nullable byte[] getRawValue() {
        return rawValue;
    }

    public void setRawValue(@Nullable byte[] rawValue) {
        this.rawValue = rawValue;
    }

    public int getParameterIndex() {
        return metadata.getParameterIndex();
    }

    public String getParameterType() {
        return metadata.getParameterType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameter parameter = (Parameter) o;

        if (!Objects.equals(value, parameter.value)) return false;
        if (!Objects.equals(metadata, parameter.metadata)) return false;
        return Arrays.equals(rawValue, parameter.rawValue);
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(rawValue);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Parameter.class.getSimpleName() + "[", "]")
                .add("value=" + value)
                .add("metadata=" + metadata)
                .add("rawValue=" + Arrays.toString(rawValue))
                .toString();
    }
}
