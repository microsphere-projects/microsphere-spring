/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.spring.config;

import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Objects;

import static org.springframework.util.Assert.notNull;

/**
 * The Spring Configuration Property class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Environment
 * @see PropertySource
 * @since 1.0.0
 */
public class ConfigurationProperty {

    /**
     * The name of the property
     */
    @NonNull
    private final String name;

    /**
     * The type of the property
     */
    @NonNull
    private Class<?> type;

    /**
     * The value of the property
     */
    @Nullable
    private Object value;

    /**
     * The default value of the property
     */
    @Nullable
    private Object defaultValue;

    /**
     * Whether the property is required
     */
    private boolean required;

    /**
     * The metadata of the property
     */
    @NonNull
    private final Metadata metadata;

    public ConfigurationProperty(String name) {
        this(name, String.class);
    }

    public ConfigurationProperty(String name, Class<?> type) {
        notNull(name, () -> "the property name must not null");
        this.name = name;
        setType(type);
        this.metadata = new Metadata();
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public Class<?> getType() {
        return type;
    }

    public void setType(@NonNull Class<?> type) {
        notNull(type, () -> "the property type must not null");
        this.type = type;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public void setValue(@Nullable Object value) {
        this.value = value;
    }

    @Nullable
    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(@Nullable Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @NonNull
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ConfigurationProperty that)) return false;

        return isRequired() == that.isRequired()
                && getName().equals(that.getName())
                && getType().equals(that.getType())
                && Objects.equals(getValue(), that.getValue())
                && Objects.equals(getDefaultValue(), that.getDefaultValue())
                && Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getType().hashCode();
        result = 31 * result + Objects.hashCode(getValue());
        result = 31 * result + Objects.hashCode(getDefaultValue());
        result = 31 * result + Boolean.hashCode(isRequired());
        result = 31 * result + Objects.hashCode(getMetadata());
        return result;
    }

    @Override
    public String toString() {
        return "ConfigurationProperty{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", value=" + value +
                ", defaultValue=" + defaultValue +
                ", required=" + required +
                ", metadata=" + metadata +
                '}';
    }

    /**
     * The metadata class of the Spring Configuration Property
     */
    public static class Metadata {

        /**
         * The description of the property
         */
        private String description;

        /**
         * The targets of the property
         */
        private List<String> targets;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getTargets() {
            return targets;
        }

        public void setTargets(List<String> targets) {
            this.targets = targets;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof Metadata metadata)) return false;

            return Objects.equals(description, metadata.description)
                    && Objects.equals(targets, metadata.targets);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(description);
            result = 31 * result + Objects.hashCode(targets);
            return result;
        }

        @Override
        public String toString() {
            return "Metadata{" +
                    "description='" + description + '\'' +
                    ", targets=" + targets +
                    '}';
        }
    }
}
