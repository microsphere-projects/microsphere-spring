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
package io.github.microsphere.spring.config.context.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;

import static io.github.microsphere.spring.util.AnnotationUtils.getAnnotationAttributes;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;
import static org.springframework.util.StringUtils.hasText;

/**
 * Abstract {@link ImportSelector} class to load the {@link PropertySource PropertySource}
 * when the {@link Configuration configuration} annotated the specified annotation
 *
 * @param <A> The type of {@link Annotation}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySourceLoader
 * @see ExtendablePropertySourceLoader
 * @see ImportSelector
 * @since 1.0.0
 */
public abstract class AnnotatedPropertySourceLoader<A extends Annotation> implements ImportSelector, EnvironmentAware, BeanFactoryAware {

    private static final String[] NO_CLASS_TO_IMPORT = new String[0];

    protected static final String NAME_ATTRIBUTE_NAME = "name";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Class<A> annotationType;

    private ConfigurableEnvironment environment;

    private ConfigurableListableBeanFactory beanFactory;

    public AnnotatedPropertySourceLoader() {
        this.annotationType = resolveAnnotationType();
    }

    private Class<A> resolveAnnotationType() {
        ResolvableType type = ResolvableType.forType(this.getClass());
        ResolvableType superType = type.as(AnnotatedPropertySourceLoader.class);
        return (Class<A>) superType.resolveGeneric(0);
    }

    @Override
    public final String[] selectImports(AnnotationMetadata metadata) {
        String annotationClassName = annotationType.getName();
        AnnotationAttributes annotationAttributes = getAnnotationAttributes(metadata, annotationClassName);
        String propertySourceName = resolvePropertySourceName(annotationAttributes, metadata);
        MutablePropertySources propertySources = environment.getPropertySources();
        try {
            loadPropertySource(annotationAttributes, metadata, propertySourceName, propertySources);
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
    @NonNull
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
    @NonNull
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

    @Override
    public final void setEnvironment(Environment environment) {
        Class<ConfigurableEnvironment> targetType = ConfigurableEnvironment.class;
        Assert.isInstanceOf(targetType, environment, "The 'environment' argument must be an instance of class " + targetType.getName());
        this.environment = targetType.cast(environment);
    }

    @Override
    public final void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        Class<ConfigurableListableBeanFactory> targetType = ConfigurableListableBeanFactory.class;
        Assert.isInstanceOf(targetType, beanFactory, "The 'beanFactory' argument must be an instance of class " + targetType.getName());
        this.beanFactory = targetType.cast(beanFactory);
    }

    /**
     * The annotation type
     *
     * @return non-null
     */
    @NonNull
    public final Class<A> getAnnotationType() {
        return annotationType;
    }

    /**
     * The {@link ConfigurableEnvironment} instance
     *
     * @return non-null
     */
    @NonNull
    public final ConfigurableEnvironment getEnvironment() {
        return environment;
    }

    /**
     * The {@link ConfigurableListableBeanFactory} instance
     *
     * @return non-null
     */
    @NonNull
    public final ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }
}
