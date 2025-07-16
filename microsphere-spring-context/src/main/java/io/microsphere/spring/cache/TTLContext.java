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

    public static void doWithTTL(Consumer<Duration> ttlFunction, Duration defaultTTL) {
        doWithTTL(ttl -> {
            ttlFunction.accept(ttl);
            return null;
        }, defaultTTL);
    }

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

    public static void setTTL(Duration ttl) {
        ttlThreadLocal.set(ttl);
    }

    public static Duration getTTL() {
        return ttlThreadLocal.get();
    }

    public static void clearTTL() {
        ttlThreadLocal.remove();
    }

}
