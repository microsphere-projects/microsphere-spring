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
package io.microsphere.spring.core.io;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.util.ClassLoaderUtils;
import io.microsphere.util.Utils;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;

/**
 * The utilities class for Spring {@link ResourceLoader}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ResourceLoader
 * @since 1.0.0
 */
public abstract class ResourceLoaderUtils implements Utils {

    private static ConcurrentMap<ClassLoader, ResourceLoader> resourceLoadersCache = newConcurrentHashMap();

    static {
        addShutdownHookCallback(ResourceLoaderUtils::clearResourceLoadersCache);
    }

    /**
     * Get the instance of {@link ResourceLoader} associating {@link ClassLoaderUtils#getDefaultClassLoader() the default ClassLoader}
     *
     * @return non-null
     */
    @Nonnull
    public static ResourceLoader getResourceLoader() {
        return getResourceLoader(getDefaultClassLoader());
    }

    /**
     * Get the instance of {@link ResourceLoader} associating the specified ClassLoader
     *
     * @param classLoader nullable {@link ClassLoader}
     * @return non-null
     */
    @Nonnull
    public static ResourceLoader getResourceLoader(@Nullable ClassLoader classLoader) {
        ClassLoader targetClassLoader = ClassLoaderUtils.nullSafeClassLoader(classLoader);
        return resourceLoadersCache.computeIfAbsent(targetClassLoader, PathMatchingResourcePatternResolver::new);
    }

    /**
     * Get the instance of {@link ResourcePatternResolver} associating {@link ClassLoaderUtils#getDefaultClassLoader() the default ClassLoader}
     *
     * @return non-null
     */
    @Nonnull
    public static ResourcePatternResolver getResourcePatternResolver() {
        return getResourcePatternResolver(null);
    }

    /**
     * Get the instance of {@link ResourcePatternResolver} from the specified {@link ResourceLoader}
     *
     * @param resourceLoader nullable {@link ResourceLoader}
     * @return non-null
     */
    @Nonnull
    public static ResourcePatternResolver getResourcePatternResolver(@Nullable ResourceLoader resourceLoader) {
        ClassLoader classLoader = nullSafeClassLoader(resourceLoader);
        return (ResourcePatternResolver) resourceLoadersCache.computeIfAbsent(classLoader, cl -> {
            final ResourcePatternResolver resourcePatternResolver;
            if (resourceLoader instanceof ResourcePatternResolver resolver) {
                resourcePatternResolver = resolver;
            } else {
                resourcePatternResolver = new PathMatchingResourcePatternResolver(cl);
            }
            return resourcePatternResolver;
        });
    }

    /**
     * Get the {@link ClassLoader} from the specified {@link ResourceLoader}.
     * <p>
     * If the {@code resourceLoader} is {@code null}, returns {@code null}.
     * Otherwise, retrieves the {@link ClassLoader} via {@link ResourceLoader#getClassLoader()}.
     *
     * @param resourceLoader the {@link ResourceLoader} instance, may be {@code null}
     * @return the associated {@link ClassLoader}, or {@code null} if the {@code resourceLoader} is {@code null}
     * @see ResourceLoader#getClassLoader()
     */
    @Nullable
    public static ClassLoader getClassLoader(@Nullable ResourceLoader resourceLoader) {
        return resourceLoader == null ? null : resourceLoader.getClassLoader();
    }

    /**
     * Get the null-safe {@link ClassLoader} from the specified {@link ResourceLoader}.
     * <p>
     * If the {@code resourceLoader} is {@code null}, or its associated {@link ClassLoader} is {@code null},
     * returns the default {@link ClassLoader} via {@link ClassLoaderUtils#getDefaultClassLoader()}.
     * Otherwise, returns the {@link ClassLoader} associated with the {@code resourceLoader}.
     *
     * @param resourceLoader the {@link ResourceLoader} instance, may be {@code null}
     * @return the non-null {@link ClassLoader}
     * @see #getClassLoader(ResourceLoader)
     * @see ClassLoaderUtils#nullSafeClassLoader(ClassLoader)
     */
    @Nonnull
    public static ClassLoader nullSafeClassLoader(@Nullable ResourceLoader resourceLoader) {
        ClassLoader classLoader = getClassLoader(resourceLoader);
        return ClassLoaderUtils.nullSafeClassLoader(classLoader);
    }

    static void clearResourceLoadersCache() {
        resourceLoadersCache.clear();
    }

    private ResourceLoaderUtils() {
    }
}