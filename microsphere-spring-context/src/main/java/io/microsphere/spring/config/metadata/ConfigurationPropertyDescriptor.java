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
package io.microsphere.spring.config.metadata;

import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.reflect.Type;
import java.util.Objects;

import static org.springframework.util.Assert.notNull;

/**
 * The descriptor class of the Spring Configuration Property
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Environment
 * @see PropertySource
 * @since 1.0.0
 */
public class ConfigurationPropertyDescriptor {

    @NonNull
    private String name;

    @NonNull
    private Type type;

    @Nullable
    private Object value;

    @Nullable
    private Object defaultValue;

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        notNull(name, () -> "the property name must not null");
        this.name = name;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    public void setType(@NonNull Type type) {
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConfigurationPropertyDescriptor that)) return false;
        return Objects.equals(name, that.name)
                && Objects.equals(type, that.type)
                && Objects.equals(value, that.value)
                && Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, value, defaultValue);
    }

    @Override
    public String toString() {
        return "ConfigurationPropertyDescriptor{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", value=" + value +
                ", defaultValue=" + defaultValue +
                '}';
    }
}
