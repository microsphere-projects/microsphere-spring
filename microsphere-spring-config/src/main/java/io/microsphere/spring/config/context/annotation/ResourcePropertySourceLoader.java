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
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * The Internal {@link AnnotatedPropertySourceLoader} Class for {@link ResourcePropertySource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySource
 * @see ResourcePropertySourceAttributes
 * @see YamlPropertySourceFactory
 * @since 1.0.0
 */
public class ResourcePropertySourceLoader<A extends Annotation, EA extends ResourcePropertySourceAttributes<A>>
        extends ExtensiblePropertySourceLoader<A, EA> implements ResourceLoaderAware, BeanClassLoaderAware {

    private ResourcePatternResolver resourcePatternResolver;

    private ClassLoader classLoader;

    @Override
    protected Class<A> resolveAnnotationType() {
        Class<A> annotationType = super.resolveAnnotationType();
        if (PropertySourceExtension.class.equals(annotationType)) {
            annotationType = (Class<A>) ResourcePropertySource.class;
        }
        return annotationType;
    }

    /**
     * Resolve the given location pattern into Resource objects.
     * <p>
     * The subclass can override this method for customization
     *
     * @param extensionAttributes
     * @param propertySourceName
     * @param resourceValue       the resource location to resolve
     * @return non-null
     * @throws Throwable
     */
    protected Resource[] getResources(EA extensionAttributes, String propertySourceName, String resourceValue)
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
