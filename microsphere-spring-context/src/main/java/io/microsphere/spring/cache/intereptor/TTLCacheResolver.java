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

import io.microsphere.collection.MapUtils;
import io.microsphere.spring.cache.annotation.TTLCachePut;
import io.microsphere.spring.cache.annotation.TTLCacheable;
import io.microsphere.util.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CachePutOperation;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.CacheableOperation;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.microsphere.spring.util.AnnotationUtils.getAnnotationAttributes;
import static java.time.Duration.ofMillis;
import static java.util.Collections.emptyList;

/**
 * TTL Customized {@link CacheResolver}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class TTLCacheResolver implements CacheResolver, EnvironmentAware {

    public static final String BEAN_NAME = "ttlCacheResolver";

    private static final Logger logger = LoggerFactory.getLogger(TTLCacheResolver.class);

    private static final Class<? extends Annotation>[] TTL_ANNOTATION_TYPES = ArrayUtils.of(TTLCacheable.class, TTLCachePut.class);

    private static final Map<Class<? extends CacheOperation>, Class<? extends Annotation>> ttlAnnotationTypesMapping = (Map) MapUtils.of(
            CacheableOperation.class, TTLCacheable.class,
            CachePutOperation.class, TTLCachePut.class
    );

    private static final ThreadLocal<Duration> ttlThreadLocal = new ThreadLocal<>();

    private final ObjectProvider<Map<String, CacheManager>> namedCacheManagerProvider;

    private Environment environment;

    public TTLCacheResolver(ObjectProvider<Map<String, CacheManager>> namedCacheManagerProvider) {
        this.namedCacheManagerProvider = namedCacheManagerProvider;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        CacheOperation cacheOperation = (CacheOperation) context.getOperation();

        Set<String> cacheNames = cacheOperation.getCacheNames();
        int cacheNamesSize = cacheNames.size();
        if (cacheNamesSize < 1) {
            return emptyList();
        }

        Map<String, CacheManager> namedCacheManagersMap = namedCacheManagerProvider.getIfAvailable();
        int cacheManagersSize = namedCacheManagersMap == null ? 0 : namedCacheManagersMap.size();
        if (cacheManagersSize < 1) {
            return emptyList();
        }

        int cachesSize = cacheNamesSize * cacheManagersSize;

        Collection<Cache> caches = new ArrayList<>(cachesSize);

        AnnotationAttributes ttlAnnotationAttributes = getTTLAnnotationAttributes(context, cacheOperation);
        try {
            Duration ttl = getTTL(ttlAnnotationAttributes);
            Collection<CacheManager> targetCacheManagers;
            setTTL(ttl);
            String[] cacheManagerBeanNames = ttlAnnotationAttributes.getStringArray("cacheManagers");
            if (ArrayUtils.isEmpty(cacheManagerBeanNames)) {
                targetCacheManagers = namedCacheManagersMap.values();
            } else {
                targetCacheManagers = new LinkedList<>();
                for (String cacheManagerBeanName : cacheManagerBeanNames) {
                    CacheManager cacheManager = namedCacheManagersMap.get(cacheManagerBeanName);
                    targetCacheManagers.add(cacheManager);
                }
            }
            for (CacheManager cacheManager : targetCacheManagers) {
                for (String cacheName : cacheNames) {
                    Cache cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        caches.add(cache);
                    }
                }
            }
        } finally {
            clearTTL();
        }

        return caches;
    }

    private AnnotationAttributes getTTLAnnotationAttributes(CacheOperationInvocationContext<?> context, CacheOperation cacheOperation) {
        Class<?> cacheOperationClass = cacheOperation.getClass();

        Class<? extends Annotation> annotationType = ttlAnnotationTypesMapping.get(cacheOperationClass);
        if (annotationType == null) {
            return null;
        }

        Method method = context.getMethod();

        AnnotatedElement annotatedElement = method;

        AnnotationAttributes attributes = getAnnotationAttributes(annotatedElement, annotationType, environment, false);
        if (attributes == null) {
            annotatedElement = context.getTarget() == null ? method.getDeclaringClass() : context.getTarget().getClass();
            attributes = getAnnotationAttributes(annotatedElement, annotationType, environment, false);
        }

        return attributes;
    }

    private Duration getTTL(AnnotationAttributes attributes) {
        long expire = (Long) attributes.get("expire");
        TimeUnit timeUnit = (TimeUnit) attributes.get("timeUnit");
        Duration ttl = ofMillis(timeUnit.toMillis(expire));
        return ttl;
    }

    private List<AnnotationAttributes> getTTLAnnotationAttributesList(CacheOperationInvocationContext<?> context) {
        Method method = context.getMethod();
        int size = TTL_ANNOTATION_TYPES.length;
        List<AnnotationAttributes> annotationAttributesList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Class<? extends Annotation> annotationType = TTL_ANNOTATION_TYPES[i];
            AnnotationAttributes annotationAttributes = getAnnotationAttributes(method, annotationType, environment, false);
            annotationAttributesList.add(annotationAttributes);
        }
        return annotationAttributesList;
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

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
