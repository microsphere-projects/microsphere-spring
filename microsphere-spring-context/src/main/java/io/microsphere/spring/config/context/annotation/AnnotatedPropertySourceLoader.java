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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.spring.context.annotation.BeanCapableImportCandidate;
import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.Map;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes.of;
import static io.microsphere.util.StringUtils.EMPTY_STRING_ARRAY;
import static org.springframework.core.ResolvableType.forType;
import static org.springframework.util.StringUtils.hasText;

/**
 * Abstract base class for {@link ImportSelector} implementations that load a {@link PropertySource}
 * when a {@link Configuration} class is annotated with a specific annotation.
 *
 * <p>This class provides a foundation for conditionally adding property sources to the Spring environment
 * based on annotations present on configuration classes. Subclasses must implement the
 * {@link #loadPropertySource(AnnotationAttributes, AnnotationMetadata, String, MutablePropertySources)} method
 * to define how the property source is loaded.
 *
 * <h3>Example Usage</h3>
 *
 * <pre>{@code
 * public class MyPropertySourceLoader extends AnnotatedPropertySourceLoader<MyPropertySource> {
 *
 *     @Override
 *     protected void loadPropertySource(AnnotationAttributes attributes,
 *                                       AnnotationMetadata metadata,
 *                                       String propertySourceName,
 *                                       MutablePropertySources propertySources) throws Throwable {
 *         // Create and add a custom PropertySource
 *         PropertySource<?> propertySource = new CustomPropertySource(propertySourceName);
 *         propertySources.addLast(propertySource);
 *     }
 * }
 * }</pre>
 *
 * @param <A> The type of annotation that triggers property source loading
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySourceLoader
 * @see PropertySourceExtensionLoader
 * @see ImportSelector
 * @since 1.0.0
 */
public abstract class AnnotatedPropertySourceLoader<A extends Annotation> extends BeanCapableImportCandidate
        implements ImportSelector {

    private static final String[] NO_CLASS_TO_IMPORT = EMPTY_STRING_ARRAY;

    protected static final String NAME_ATTRIBUTE_NAME = "name";

    protected final Logger logger = getLogger(this.getClass());

    private final Class<A> annotationType;

    private String propertySourceName;

    public AnnotatedPropertySourceLoader() {
        this.annotationType = resolveAnnotationType();
    }

    protected Class<A> resolveAnnotationType() {
        ResolvableType type = forType(this.getClass());
        ResolvableType superType = type.as(AnnotatedPropertySourceLoader.class);
        return (Class<A>) superType.resolveGeneric(0);
    }

    @Override
    public final String[] selectImports(AnnotationMetadata metadata) {
        String annotationClassName = annotationType.getName();
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(annotationClassName);
        ResolvablePlaceholderAnnotationAttributes attributes = of(annotationAttributes, annotationType, getEnvironment());
        String propertySourceName = resolvePropertySourceName(attributes, metadata);
        this.propertySourceName = propertySourceName;
        MutablePropertySources propertySources = getEnvironment().getPropertySources();
        try {
            loadPropertySource(attributes, metadata, propertySourceName, propertySources);
        } catch (Throwable e) {
            String errorMessage = "The Configuration bean[class : '" + metadata.getClassName() + "', annotated : @" + annotationClassName + "] can't load the PropertySource[name : '" + propertySourceName + "']";
            logger.error(errorMessage, e);
            throw new BeanCreationException(errorMessage, e);
        }
        return NO_CLASS_TO_IMPORT;
    }


    /**
     * Resolve the name of {@link PropertySource}
     *
     * @param attributes {@link AnnotationAttributes}
     * @param metadata   {@link AnnotationMetadata}
     * @return non-null
     */
    @Nonnull
    protected final String resolvePropertySourceName(AnnotationAttributes attributes, AnnotationMetadata metadata) {
        String name = buildPropertySourceName(attributes, metadata);
        if (!hasText(name)) {
            name = buildDefaultPropertySourceName(attributes, metadata);
        }
        return name;
    }

    /**
     * Build the name of {@link PropertySource}
     *
     * @param attributes {@link AnnotationAttributes}
     * @param metadata   {@link AnnotationMetadata}
     * @return the attribute value of annotation if the {@link #NAME_ATTRIBUTE_NAME "name"} attribute present, or <code>null</code>
     */
    @Nullable
    protected String buildPropertySourceName(AnnotationAttributes attributes, AnnotationMetadata metadata) {
        if (attributes.containsKey(NAME_ATTRIBUTE_NAME)) {
            return attributes.getString(NAME_ATTRIBUTE_NAME);
        }
        return null;
    }


    /**
     * Build the default name of {@link PropertySource}
     *
     * @param attributes {@link AnnotationAttributes}
     * @param metadata   {@link AnnotationMetadata}
     * @return non-null
     */
    @Nonnull
    protected String buildDefaultPropertySourceName(AnnotationAttributes attributes, AnnotationMetadata metadata) {
        String annotationClassName = annotationType.getName();
        String introspectedClassName = metadata.getClassName();
        return introspectedClassName + "@" + annotationClassName;
    }

    /**
     * Load the {@link PropertySource}
     *
     * @param attributes         {@link AnnotationAttributes}
     * @param metadata           {@link AnnotationMetadata}
     * @param propertySourceName the name of {@link PropertySource}
     * @param propertySources    {@link MutablePropertySources} to be added
     * @throws Throwable the failure of the loading
     */
    @Nullable
    protected abstract void loadPropertySource(AnnotationAttributes attributes, AnnotationMetadata metadata,
                                               String propertySourceName, MutablePropertySources propertySources) throws Throwable;

    /**
     * The annotation type
     *
     * @return non-null
     */
    @Nonnull
    public final Class<A> getAnnotationType() {
        return annotationType;
    }

    protected String getPropertySourceName() {
        return this.propertySourceName;
    }
}
