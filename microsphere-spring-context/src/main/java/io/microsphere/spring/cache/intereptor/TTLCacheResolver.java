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
package io.microsphere.spring.cache.intereptor;

import io.microsphere.spring.cache.annotation.TTLCacheable;
import io.microsphere.spring.cache.redis.ConfigurableRedisCacheManager;
import io.microsphere.util.ArrayUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.BasicOperation;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;

/**
 * TTL Customized {@link CacheResolver}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class TTLCacheResolver implements CacheResolver, ApplicationContextAware {

    public static final String BEAN_NAME = "ttlCacheResolver";

    private ApplicationContext context;

    private final ObjectProvider<Map<String, CacheManager>> namedCacheManagerProvider;

    public TTLCacheResolver(ObjectProvider<Map<String, CacheManager>> namedCacheManagerProvider) {
        this.namedCacheManagerProvider = namedCacheManagerProvider;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {

        Collection<Cache> caches = new LinkedList<>();

        namedCacheManagerProvider.ifAvailable(namedCacheManagersMap -> {

            Method method = context.getMethod();
            BasicOperation operation = context.getOperation();

            TTLCacheable ttlCacheable = method.getAnnotation(TTLCacheable.class);
            if (ttlCacheable != null) {
                Collection<CacheManager> targetCacheManagers = emptyList();
                long expire = ttlCacheable.expire();
                TimeUnit timeUnit = ttlCacheable.timeUnit();
                Duration ttl = Duration.ofMillis(timeUnit.toMillis(expire));
                ConfigurableRedisCacheManager.setTTL(ttl);
                Set<String> cacheNames = operation.getCacheNames();
                String[] cacheManagerBeanNames = ttlCacheable.cacheManagerBeanNames();
                if (ArrayUtils.isEmpty(cacheManagerBeanNames)) {
                    targetCacheManagers = namedCacheManagersMap.values();
                } else {
                    targetCacheManagers = new LinkedList<>();
                    for (String cacheManagerBeanName : cacheManagerBeanNames) {
                        CacheManager cacheManager = namedCacheManagersMap.get(cacheManagerBeanName);
                        targetCacheManagers.add(cacheManager);
                    }
                }
                // targetCacheManagers 可能包含 local + remote CacheManager
                for (CacheManager cacheManager : targetCacheManagers) {
                    for (String cacheName : cacheNames) {
                        Cache cache = cacheManager.getCache(cacheName);
                        if (cache != null) {
                            caches.add(cache);
                        }
                    }
                }
            }
            ConfigurableRedisCacheManager.clearTTL();
        });

        return caches;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
