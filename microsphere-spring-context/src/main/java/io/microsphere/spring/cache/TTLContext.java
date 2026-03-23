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
package io.microsphere.spring.cache;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A context class that manages Time-To-Live (TTL) values using a thread-local variable.
 * This class provides utility methods to execute operations with a specified TTL,
 * supporting both void and return-value operations via functional interfaces.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Using doWithTTL with a Consumer to handle TTL value
 * TTLContext.doWithTTL(ttl -> {
 *     System.out.println("Current TTL: " + ttl);
 * }, Duration.ofSeconds(30));
 *
 * // Using doWithTTL with a Function to handle TTL and return a result
 * String result = TTLContext.doWithTTL(ttl -> {
 *     return "TTL is: " + ttl;
 * }, Duration.ofSeconds(60));
 *
 * // Setting TTL manually
 * TTLContext.setTTL(Duration.ofMinutes(5));
 * Duration currentTTL = TTLContext.getTTL();
 *
 * // Clearing TTL manually
 * TTLContext.clearTTL();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class TTLContext {

    private static final ThreadLocal<Duration> ttlThreadLocal = new ThreadLocal<>();

    /**
     * Executes the given {@link Consumer} with the effective TTL value. If a TTL has been
     * previously set via {@link #setTTL(Duration)}, that value is used; otherwise, the
     * provided {@code defaultTTL} is used. The TTL is cleared after the consumer completes.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   doWithTTL(d -> {
     *       System.out.println("TTL duration: " + d);
     *   }, Duration.ofMillis(10));
     * }</pre>
     *
     * @param ttlFunction the consumer to execute with the effective TTL duration
     * @param defaultTTL  the default TTL to use if no TTL has been set via {@link #setTTL(Duration)}
     */
    public static void doWithTTL(Consumer<Duration> ttlFunction, Duration defaultTTL) {
        doWithTTL(ttl -> {
            ttlFunction.accept(ttl);
            return null;
        }, defaultTTL);
    }

    /**
     * Executes the given {@link Function} with the effective TTL value and returns its result.
     * If a TTL has been previously set via {@link #setTTL(Duration)}, that value is used;
     * otherwise, the provided {@code defaultTTL} is used. The TTL is cleared after the
     * function completes, even if an exception is thrown.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Duration duration = Duration.ofMillis(10);
     *   Duration result = doWithTTL(d -> {
     *       return d;
     *   }, duration);
     * }</pre>
     *
     * @param <R>         the type of the result returned by the function
     * @param ttlFunction the function to execute with the effective TTL duration
     * @param defaultTTL  the default TTL to use if no TTL has been set via {@link #setTTL(Duration)}
     * @return the result of applying the function to the effective TTL
     */
    public static <R> R doWithTTL(Function<Duration, R> ttlFunction, Duration defaultTTL) {
        Duration effectiveTTL = getEffectiveTTL(defaultTTL);
        try {
            return ttlFunction.apply(effectiveTTL);
        } finally {
            clearTTL();
        }
    }

    private static Duration getEffectiveTTL(Duration defaultTTL) {
        Duration ttl = getTTL();
        return ttl == null ? defaultTTL : ttl;
    }

    /**
     * Sets the TTL value for the current thread. This value will take precedence over the
     * {@code defaultTTL} parameter in subsequent calls to
     * {@link #doWithTTL(Consumer, Duration)} or {@link #doWithTTL(Function, Duration)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Duration duration = Duration.ofMillis(100);
     *   setTTL(duration);
     *   doWithTTL(d -> {
     *       // d will be the previously set duration (100ms), not the default (10ms)
     *       assertEquals(d, duration);
     *   }, Duration.ofMillis(10));
     * }</pre>
     *
     * @param ttl the TTL duration to set for the current thread
     */
    public static void setTTL(Duration ttl) {
        ttlThreadLocal.set(ttl);
    }

    /**
     * Returns the TTL value that has been set for the current thread, or {@code null} if
     * no TTL has been set or it has been cleared.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Duration duration = Duration.ofMillis(100);
     *   setTTL(duration);
     *   assertEquals(duration, getTTL());
     * }</pre>
     *
     * @return the current thread's TTL duration, or {@code null} if not set
     */
    public static Duration getTTL() {
        return ttlThreadLocal.get();
    }

    /**
     * Clears the TTL value for the current thread. After calling this method,
     * {@link #getTTL()} will return {@code null}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   setTTL(Duration.ofMillis(100));
     *   clearTTL();
     *   assertNull(getTTL());
     * }</pre>
     */
    public static void clearTTL() {
        ttlThreadLocal.remove();
    }

}
