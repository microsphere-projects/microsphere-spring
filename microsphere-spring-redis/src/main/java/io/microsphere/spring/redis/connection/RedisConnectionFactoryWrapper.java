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
package io.microsphere.spring.redis.connection;

import io.microsphere.spring.redis.beans.DelegatingWrapper;
import io.microsphere.spring.redis.context.RedisContext;
import io.microsphere.spring.redis.interceptor.InterceptingRedisConnectionInvocationHandler;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * {@link RedisConnectionFactory} Wrapper
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisConnectionFactory
 * @since 1.0.0
 */
public class RedisConnectionFactoryWrapper implements RedisConnectionFactory, DelegatingWrapper {

    private static final Class[] REDIS_CONNECTION_TYPES = new Class[]{RedisConnection.class};

    private final String beanName;
    private final RedisConnectionFactory delegate;

    private final RedisContext redisContext;

    public RedisConnectionFactoryWrapper(String beanName, RedisConnectionFactory delegate, RedisContext redisContext) {
        this.beanName = beanName;
        this.delegate = delegate;
        this.redisContext = redisContext;
    }

    @Override
    public RedisConnection getConnection() {
        RedisConnection connection = delegate.getConnection();
        if (isEnabled()) {
            return newProxyRedisConnection(connection, redisContext, beanName);
        }
        return connection;
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        return delegate.getClusterConnection();
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return delegate.getConvertPipelineAndTxResults();
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        return delegate.getSentinelConnection();
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        return delegate.translateExceptionIfPossible(ex);
    }

    public boolean isEnabled() {
        return redisContext.getRedisConfiguration().isEnabled();
    }

    public static RedisConnection newProxyRedisConnection(RedisConnection connection, RedisContext redisContext, String sourceBeanName) {
        ClassLoader classLoader = redisContext.getClassLoader();
        InvocationHandler invocationHandler = newInvocationHandler(connection, redisContext, sourceBeanName);
        return (RedisConnection) Proxy.newProxyInstance(classLoader, REDIS_CONNECTION_TYPES, invocationHandler);
    }

    private static InvocationHandler newInvocationHandler(RedisConnection connection, RedisContext redisContext, String sourceBeanName) {
        return new InterceptingRedisConnectionInvocationHandler(connection, redisContext, sourceBeanName);
    }

    @Override
    public Object getDelegate() {
        return delegate;
    }
}
