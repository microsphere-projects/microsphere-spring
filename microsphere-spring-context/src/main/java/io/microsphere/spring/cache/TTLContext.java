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
     * Executes the given {@link Consumer} with the effective TTL, which is the value from the
     * current thread's {@link ThreadLocal} if set, or the provided default TTL otherwise.
     * The thread-local TTL is cleared after execution.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Uses default TTL when no thread-local TTL is set
     *   TTLContext.doWithTTL(ttl -> {
     *       System.out.println("TTL: " + ttl); // TTL: PT1M
     *   }, Duration.ofMinutes(1));
     *
     *   // Uses thread-local TTL when set
     *   TTLContext.setTTL(Duration.ofSeconds(30));
     *   TTLContext.doWithTTL(ttl -> {
     *       System.out.println("TTL: " + ttl); // TTL: PT30S
     *   }, Duration.ofMinutes(1));
     * }</pre>
     *
     * @param ttlFunction the consumer to execute with the effective TTL
     * @param defaultTTL  the default TTL to use if no thread-local TTL is set
     */
    public static void doWithTTL(Consumer<Duration> ttlFunction, Duration defaultTTL) {
        doWithTTL(ttl -> {
            ttlFunction.accept(ttl);
            return null;
        }, defaultTTL);
    }

    /**
     * Executes the given {@link Function} with the effective TTL and returns the result.
     * The effective TTL is the value from the current thread's {@link ThreadLocal} if set,
     * or the provided default TTL otherwise. The thread-local TTL is cleared after execution.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TTLContext.setTTL(Duration.ofSeconds(30));
     *   long seconds = TTLContext.doWithTTL(Duration::getSeconds, Duration.ofMinutes(1));
     *   // seconds == 30
     *
     *   // With no thread-local TTL set, uses the default
     *   long defaultSeconds = TTLContext.doWithTTL(Duration::getSeconds, Duration.ofMinutes(1));
     *   // defaultSeconds == 60
     * }</pre>
     *
     * @param ttlFunction the function to execute with the effective TTL
     * @param defaultTTL  the default TTL to use if no thread-local TTL is set
     * @param <R>         the return type of the function
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
     * Sets the TTL value in the current thread's {@link ThreadLocal} storage.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TTLContext.setTTL(Duration.ofMinutes(5));
     *   Duration ttl = TTLContext.getTTL();
     *   // ttl == Duration.ofMinutes(5)
     * }</pre>
     *
     * @param ttl the TTL duration to store for the current thread
     */
    public static void setTTL(Duration ttl) {
        ttlThreadLocal.set(ttl);
    }

    /**
     * Gets the TTL value from the current thread's {@link ThreadLocal} storage.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TTLContext.setTTL(Duration.ofMinutes(5));
     *   Duration ttl = TTLContext.getTTL(); // PT5M
     *
     *   TTLContext.clearTTL();
     *   Duration cleared = TTLContext.getTTL(); // null
     * }</pre>
     *
     * @return the TTL duration for the current thread, or {@code null} if not set
     */
    public static Duration getTTL() {
        return ttlThreadLocal.get();
    }

    /**
     * Clears the TTL value from the current thread's {@link ThreadLocal} storage.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   TTLContext.setTTL(Duration.ofMinutes(5));
     *   TTLContext.clearTTL();
     *   Duration ttl = TTLContext.getTTL(); // null
     * }</pre>
     */
    public static void clearTTL() {
        ttlThreadLocal.remove();
    }

}
