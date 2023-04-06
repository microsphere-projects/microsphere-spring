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
package io.github.microsphere.spring.resilience4j.common;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.Registry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.vavr.control.Try;
import org.springframework.core.ResolvableType;

import java.beans.Introspector;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The Resilience4j Module enumeration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public enum Resilience4jModule {

    /**
     * {@link CircuitBreaker} module
     */
    CIRCUIT_BREAKER(CircuitBreakerRegistry.class, 1),

    /**
     * {@link Bulkhead} module
     */
    BULKHEAD(BulkheadRegistry.class, 4),

    /**
     * {@link RateLimiter} module
     */
    RATE_LIMITER(RateLimiterRegistry.class, 3),

    /**
     * {@link Retry} module
     */
    RETRY(RetryRegistry.class, 0),

    /**
     * {@link TimeLimiter} module
     */
    TIME_LIMITER(TimeLimiterRegistry.class, 3);

    private final static int ENTRY_CLASS_GENERIC_INDEX = 0;

    private final static int CONFIGURATION_CLASS_GENERIC_INDEX = 1;

    private static MethodHandles.Lookup lookup;

    private final Class<?> entryClass;

    private final Class<? extends Registry> registryClass;

    private final Class<?> configClass;

    private final MethodHandle entryMethodHandle;

    /**
     * @see <a href="https://resilience4j.readme.io/docs/getting-started-3#aspect-order">Resilience4j Aspect order</a>
     */
    private final int defaultAspectOrder;


    Resilience4jModule(Class<? extends Registry> registryClass, int defaultAspectOrder) {
        this(getEntryClass(registryClass), registryClass, getConfigurationClass(registryClass), defaultAspectOrder);
    }

    Resilience4jModule(Class<?> entryClass, Class<? extends Registry> registryClass, Class<?> configClass, int defaultAspectOrder) {
        this(entryClass, registryClass, configClass, Arrays.asList(String.class, configClass), defaultAspectOrder);
    }

    Resilience4jModule(Class<?> entryClass, Class<? extends Registry> registryClass, Class<?> configClass, List<Class<?>> entryMethodParameterTypes, int defaultAspectOrder) {
        this.entryClass = entryClass;
        this.registryClass = registryClass;
        this.configClass = configClass;
        this.entryMethodHandle = findEntryMethodHandle(entryClass, registryClass, entryMethodParameterTypes);
        this.defaultAspectOrder = defaultAspectOrder;
    }

    private static Class<?> getEntryClass(Class<? extends Registry> registryClass) {
        return getActualTypeArgument(registryClass, ENTRY_CLASS_GENERIC_INDEX);
    }

    private static Class<?> getConfigurationClass(Class<? extends Registry> registryClass) {
        return getActualTypeArgument(registryClass, CONFIGURATION_CLASS_GENERIC_INDEX);
    }

    private static Class<?> getActualTypeArgument(Class<? extends Registry> registryClass, int genericIndex) {
        ResolvableType type = ResolvableType.forClass(registryClass);
        type = type.as(Registry.class);
        return type.getGenerics()[genericIndex].resolve();
    }

    private static MethodHandle findEntryMethodHandle(Class<?> entryClass, Class<? extends Registry> registryClass, List<Class<?>> entryMethodParameterTypes) {
        MethodHandles.Lookup lookup = getLookup();
        MethodType methodType = MethodType.methodType(entryClass, entryMethodParameterTypes);
        Method method = null;
        String methodName = Introspector.decapitalize(entryClass.getSimpleName());
        final MethodHandle methodHandle;
        try {
            methodHandle = lookup.findVirtual(registryClass, methodName, methodType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return methodHandle;
    }

    private static MethodHandles.Lookup getLookup() {
        if (lookup == null) {
            lookup = MethodHandles.lookup();
        }
        return lookup;
    }

    public <R extends Registry<E, C>, E, C> E getEntry(R registry, String name) {
        C configuration = (C) getConfiguration(registry, name);
        return (E) Try.of(() -> entryMethodHandle.invoke(registry, name, configuration)).getOrElseThrow(e -> new RuntimeException(e));
    }

    private Object getConfiguration(Registry registry, String name) {
        Optional<Object> configurationProvider = registry.getConfiguration(name);
        Object configuration = configurationProvider.orElseGet(registry::getDefaultConfig);
        return configuration;
    }

    /**
     * Get the class of Resilience4j's entry
     *
     * @return non-null
     */
    public Class<?> getEntryClass() {
        return entryClass;
    }

    /**
     * Get the class of Resilience4j's {@link Registry registry}
     *
     * @return non-null
     */
    public Class<? extends Registry> getRegistryClass() {
        return registryClass;
    }

    /**
     * Get the class of Resilience4j's config
     *
     * @return non-null
     */
    public Class<?> getConfigClass() {
        return configClass;
    }

    /**
     * Get the default order of Resilience4j's entry as an aspect
     *
     * @return non-null
     * @see <a href="https://resilience4j.readme.io/docs/getting-started-3#aspect-order">Resilience4j Aspect order</a>
     */
    public int getDefaultAspectOrder() {
        return defaultAspectOrder;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Resilience4jModule{");
        sb.append("entryClass=").append(entryClass);
        sb.append(", registryClass=").append(registryClass);
        sb.append(", configClass=").append(configClass);
        sb.append(", defaultAspectOrder=").append(defaultAspectOrder);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Search the {@link Resilience4jModule} by the specified type
     *
     * @param type the type to search, may be one of the following:
     *             <ul>
     *                 <li>{@link #getEntryClass() the entry class}</li>
     *                 <li>{@link #getRegistryClass() the entry registry class}</li>
     *                 <li>{@link #getConfigClass() the configuration class}</li>
     *             </ul>
     * @return the {@link Resilience4jModule} member if found
     */
    public static Resilience4jModule valueOf(Class<?> type) {
        Resilience4jModule module = null;
        for (Resilience4jModule m : values()) {
            if (Objects.equals(type, m.getEntryClass()) || Objects.equals(type, m.getRegistryClass()) || Objects.equals(type, m.getConfigClass())) {
                module = m;
                break;
            }
        }
        if (module == null) {
            throw new IllegalArgumentException("The 'type' can't be found in Resilience4jModule : " + type.getName());
        }
        return module;
    }
}
