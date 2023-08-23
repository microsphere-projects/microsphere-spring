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
package io.microsphere.spring.cache.interceptor;

import io.microsphere.spring.cache.annotation.TTLCacheable;
import org.springframework.cache.interceptor.CacheableOperation;

import java.util.concurrent.TimeUnit;

/**
 * {@link TTLCacheable @TTLCacheable}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since TODO
 */
public class TTLCacheableOperation extends CacheableOperation {

    private long expire;

    private TimeUnit timeUnit;

    /**
     * Create a new {@link CacheableOperation} instance from the given builder.
     *
     * @param b
     * @since 4.3
     */
    public TTLCacheableOperation(Builder b) {
        super(b);
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
