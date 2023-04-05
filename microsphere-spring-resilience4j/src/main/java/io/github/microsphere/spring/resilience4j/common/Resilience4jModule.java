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
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;

import javax.cache.Cache;
import java.util.Objects;

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
    CIRCUIT_BREAKER(CircuitBreaker.class, Integer.valueOf(1)),

    /**
     * {@link Bulkhead} module
     */
    BULKHEAD(Bulkhead.class, Integer.valueOf(4)),

    /**
     * {@link RateLimiter} module
     */
    RATE_LIMITER(RateLimiter.class, Integer.valueOf(2)),

    /**
     * {@link Retry} module
     */
    RETRY(Retry.class, Integer.valueOf(0)),

    /**
     * {@link TimeLimiter} module
     */
    TIME_LIMITER(TimeLimiter.class, Integer.valueOf(3)),

    /**
     * {@link Cache} module
     */
    CACHE(Cache.class, null);

    private final Class<?> type;

    /**
     * @see <a href="https://resilience4j.readme.io/docs/getting-started-3#aspect-order">Resilience4j Aspect order</a>
     */
    private final Integer aspectOrder;

    Resilience4jModule(Class<?> type, Integer aspectOrder) {
        this.type = type;
        this.aspectOrder = aspectOrder;
    }

    public Class<?> getType() {
        return type;
    }

    public Integer getAspectOrder() {
        return aspectOrder;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Resilience4jModule{");
        sb.append("type=").append(type.getSimpleName());
        sb.append(", aspectOrder=").append(getAspectOrder());
        sb.append('}');
        return sb.toString();
    }

    public static Resilience4jModule valueOf(Class<?> type) {
        Resilience4jModule module = null;
        for (Resilience4jModule m : values()) {
            if (Objects.equals(type, m.getType())) {
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
