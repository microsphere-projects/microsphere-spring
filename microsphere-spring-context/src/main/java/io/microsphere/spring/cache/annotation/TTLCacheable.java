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
package io.microsphere.spring.cache.annotation;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static io.microsphere.spring.cache.intereptor.TTLCacheResolver.BEAN_NAME;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * A variation of {@link Cacheable @Cacheable} that supports Time-To-Live (TTL) settings for cached entries.
 *
 * <p>This annotation allows the specification of an expiration time for cache entries, making it possible
 * to control how long values should be kept in the cache before being considered stale and evicted.
 *
 * <p><b>Example:</b>
 * <pre>
 *     {@code @TTLCacheable(cacheNames = "userCache", expire = 10, timeUnit = TimeUnit.SECONDS)}
 *     public User getUser(String userId) {
 *         // Method implementation
 *     }
 * </pre>
 *
 * <p>In the example above, the result of the method will be stored in the "userCache" cache for 10 seconds.
 * After that time, the entry will be removed, and the next invocation will cause the method to be executed
 * and the result cached again.
 *
 * <p>It is typically used in Spring applications where fine-grained control over cache expiration is needed.
 * The TTL behavior is handled by the associated cache resolver, which must support TTL-based eviction policies.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Inherited
@Documented
@Cacheable(cacheResolver = BEAN_NAME)
public @interface TTLCacheable {

    /**
     * Alias for {@link #cacheNames}.
     */
    @AliasFor(annotation = Cacheable.class)
    String[] value() default {};

    /**
     * Names of the caches in which method invocation results are stored.
     * <p>Names may be used to determine the target cache (or caches), matching
     * the qualifier value or bean name of a specific bean definition.
     *
     * @see #value
     * @see CacheConfig#cacheNames
     * @since 4.2
     */
    @AliasFor(annotation = Cacheable.class)
    String[] cacheNames() default {};

    /**
     * Spring Expression Language (SpEL) expression for computing the key dynamically.
     * <p>Default is {@code ""}, meaning all method parameters are considered as a key,
     * unless a custom {@link #keyGenerator} has been configured.
     * <p>The SpEL expression evaluates against a dedicated context that provides the
     * following meta-data:
     * <ul>
     * <li>{@code #root.method}, {@code #root.target}, and {@code #root.caches} for
     * references to the {@link java.lang.reflect.Method method}, target object, and
     * affected cache(s) respectively.</li>
     * <li>Shortcuts for the method name ({@code #root.methodName}) and target class
     * ({@code #root.targetClass}) are also available.
     * <li>Method arguments can be accessed by index. For instance the second argument
     * can be accessed via {@code #root.args[1]}, {@code #p1} or {@code #a1}. Arguments
     * can also be accessed by name if that information is available.</li>
     * </ul>
     */
    @AliasFor(annotation = Cacheable.class)
    String key() default "";

    /**
     * The bean name of the custom {@link org.springframework.cache.interceptor.KeyGenerator}
     * to use.
     * <p>Mutually exclusive with the {@link #key} attribute.
     *
     * @see CacheConfig#keyGenerator
     */
    @AliasFor(annotation = Cacheable.class)
    String keyGenerator() default "";

    /**
     * The bean names of the custom {@link org.springframework.cache.CacheManager}
     */
    String[] cacheManagers() default {};

    /**
     * Spring Expression Language (SpEL) expression used for making the method
     * caching conditional. Cache the result if the condition evaluates to
     * {@code true}.
     * <p>Default is {@code ""}, meaning the method result is always cached.
     * <p>The SpEL expression evaluates against a dedicated context that provides the
     * following meta-data:
     * <ul>
     * <li>{@code #root.method}, {@code #root.target}, and {@code #root.caches} for
     * references to the {@link java.lang.reflect.Method method}, target object, and
     * affected cache(s) respectively.</li>
     * <li>Shortcuts for the method name ({@code #root.methodName}) and target class
     * ({@code #root.targetClass}) are also available.
     * <li>Method arguments can be accessed by index. For instance the second argument
     * can be accessed via {@code #root.args[1]}, {@code #p1} or {@code #a1}. Arguments
     * can also be accessed by name if that information is available.</li>
     * </ul>
     */
    @AliasFor(annotation = Cacheable.class)
    String condition() default "";

    /**
     * Spring Expression Language (SpEL) expression used to veto method caching.
     * Veto caching the result if the condition evaluates to {@code true}.
     * <p>Unlike {@link #condition}, this expression is evaluated after the method
     * has been called and can therefore refer to the {@code result}.
     * <p>Default is {@code ""}, meaning that caching is never vetoed.
     * <p>The SpEL expression evaluates against a dedicated context that provides the
     * following meta-data:
     * <ul>
     * <li>{@code #result} for a reference to the result of the method invocation. For
     * supported wrappers such as {@code Optional}, {@code #result} refers to the actual
     * object, not the wrapper</li>
     * <li>{@code #root.method}, {@code #root.target}, and {@code #root.caches} for
     * references to the {@link java.lang.reflect.Method method}, target object, and
     * affected cache(s) respectively.</li>
     * <li>Shortcuts for the method name ({@code #root.methodName}) and target class
     * ({@code #root.targetClass}) are also available.
     * <li>Method arguments can be accessed by index. For instance the second argument
     * can be accessed via {@code #root.args[1]}, {@code #p1} or {@code #a1}. Arguments
     * can also be accessed by name if that information is available.</li>
     * </ul>
     *
     * @since 3.2
     */
    @AliasFor(annotation = Cacheable.class)
    String unless() default "";

    /**
     * Synchronize the invocation of the underlying method if several threads are
     * attempting to load a value for the same key. The synchronization leads to
     * a couple of limitations:
     * <ol>
     * <li>{@link #unless()} is not supported</li>
     * <li>Only one cache may be specified</li>
     * <li>No other cache-related operation can be combined</li>
     * </ol>
     * This is effectively a hint and the actual cache provider that you are
     * using may not support it in a synchronized fashion. Check your provider
     * documentation for more details on the actual semantics.
     *
     * @see org.springframework.cache.Cache#get(Object, Callable)
     * @since 4.3
     */
    @AliasFor(annotation = Cacheable.class)
    boolean sync() default false;

    /**
     * The expire time for cacheable entry
     */
    long expire();

    /**
     * The {@link TimeUnit timeunit} of expire
     */
    TimeUnit timeUnit() default MILLISECONDS;
}
