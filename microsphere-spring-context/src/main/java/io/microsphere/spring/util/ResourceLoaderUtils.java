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
package io.microsphere.spring.util;

import io.microsphere.util.BaseUtils;
import io.microsphere.util.ClassLoaderUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;

/**
 * The utilities class for Spring {@link ResourceLoader}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ResourceLoader
 * @since 1.0.0
 */
public abstract class ResourceLoaderUtils extends BaseUtils {

    private static ConcurrentMap<ClassLoader, ResourceLoader> resourceLoadersCache = new ConcurrentHashMap<>();

    static {
        addShutdownHookCallback(resourceLoadersCache::clear);
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
        ClassLoader targetClassLoader = classLoader == null ? getDefaultClassLoader() : classLoader;
        return resourceLoadersCache.computeIfAbsent(targetClassLoader, DefaultResourceLoader::new);
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
        ClassLoader classLoader = getClassLoader(resourceLoader);
        return (ResourcePatternResolver) resourceLoadersCache.computeIfAbsent(classLoader, cl -> {
            ResourcePatternResolver resourcePatternResolver = null;
            if (resourceLoader instanceof ResourcePatternResolver) {
                resourcePatternResolver = (ResourcePatternResolver) resourceLoader;
            } else {
                resourcePatternResolver = new PathMatchingResourcePatternResolver(cl);
            }
            return resourcePatternResolver;
        });
    }

    protected static ClassLoader getClassLoader(@Nullable ResourceLoader resourceLoader) {
        ClassLoader classLoader = resourceLoader == null ? null : resourceLoader.getClassLoader();
        return classLoader == null ? getDefaultClassLoader() : classLoader;
    }

}
