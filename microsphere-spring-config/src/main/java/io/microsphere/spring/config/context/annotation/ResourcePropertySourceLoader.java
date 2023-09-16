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
package io.microsphere.spring.config.context.annotation;

import io.microsphere.spring.config.env.support.YamlPropertySourceFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

/**
 * The Internal {@link AnnotatedPropertySourceLoader} Class for {@link ResourcePropertySource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySource
 * @see ResourcePropertySourceAttributes
 * @see YamlPropertySourceFactory
 * @since 1.0.0
 */
public class ResourcePropertySourceLoader extends PropertySourceExtensionLoader<ResourcePropertySource, PropertySourceExtensionAttributes<ResourcePropertySource>> implements ResourceLoaderAware, BeanClassLoaderAware {

    private ResourcePatternResolver resourcePatternResolver;

    private ClassLoader classLoader;

    /**
     * Resolve the given location pattern into Resource objects.
     * <p>
     * The subclass can override this method for customization
     *
     * @param extensionAttributes {@link PropertySourceExtensionAttributes} for {@link ResourcePropertySource @ResourcePropertySource}
     * @param propertySourceName  {@link ResourcePropertySource#name()}
     * @param resourceValue       the resource value to resolve
     * @return non-null
     * @throws Throwable
     */
    protected Resource[] getResources(PropertySourceExtensionAttributes<ResourcePropertySource> extensionAttributes, String propertySourceName, String resourceValue)
            throws Throwable {
        return resourcePatternResolver.getResources(resourceValue);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
