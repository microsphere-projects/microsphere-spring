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
package io.microsphere.spring.redis.serializer;

import io.microsphere.spring.redis.beans.DelegatingWrapper;
import io.microsphere.spring.redis.util.ValueHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

/**
 * {@link ValueHolder} {@link RedisSerializer} Wrapper
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class HoldingValueRedisSerializerWrapper<T> implements RedisSerializer<T>, DelegatingWrapper {

    private final RedisSerializer<T> delegate;

    public HoldingValueRedisSerializerWrapper(RedisSerializer<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    @Nullable
    public byte[] serialize(T value) throws SerializationException {
        // Try to find the ThreadLocal cached result
        ValueHolder valueHolder = ValueHolder.get();
        byte[] rawValue = valueHolder.getRawValue(value);
        if (rawValue == null) {
            rawValue = delegate.serialize(value);
            // Cache the first time serialization
            valueHolder.set(value, rawValue);
        }
        return rawValue;
    }

    @Override
    @Nullable
    public T deserialize(byte[] bytes) throws SerializationException {
        // Try to find the ThreadLocal cached result
        ValueHolder valueHolder = ValueHolder.get();
        T value = (T) valueHolder.getValue(bytes);
        if (value == null) {
            value = delegate.deserialize(bytes);
            valueHolder.set(value, bytes);
        }
        return value;
    }

    @Override
    public boolean canSerialize(Class type) {
        return delegate.canSerialize(type);
    }

    @Override
    public Class<?> getTargetType() {
        return delegate.getTargetType();
    }

    @Override
    public Object getDelegate() {
        return delegate;
    }

    public static <T> RedisSerializer<T> wrap(RedisSerializer<T> redisSerializer) {
        if (redisSerializer == null) {
            return null;
        }
        return new HoldingValueRedisSerializerWrapper<>(redisSerializer);
    }

    public static void wrap(RedisTemplate redisTemplate) {
        redisTemplate.setDefaultSerializer(wrap(redisTemplate.getDefaultSerializer()));
        redisTemplate.setKeySerializer(wrap(redisTemplate.getKeySerializer()));
        redisTemplate.setValueSerializer(wrap(redisTemplate.getValueSerializer()));
        redisTemplate.setHashKeySerializer(wrap(redisTemplate.getHashKeySerializer()));
        redisTemplate.setHashValueSerializer(wrap(redisTemplate.getHashValueSerializer()));
    }
}