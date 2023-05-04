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
package io.github.microsphere.spring.config.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
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
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;

import static io.github.microsphere.spring.util.AnnotatedBeanDefinitionRegistryUtils.resolveAnnotatedBeanNameGenerator;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * Abstract {@link ImportSelector} class to load the {@link PropertySource PropertySource}
 * when the {@link Configuration configuration} annotated the Enable annotation that meta-annotates {@link EnableConfig @EnableConfig}
 *
 * @param <A> The type of {@link Annotation} must meta-annotate {@link EnableConfig}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableConfig
 * @see EnableConfigAttributes
 * @see ImportSelector
 * @since 1.0.0
 */
public abstract class EnableConfigPropertySourceLoader<A extends Annotation> implements ImportSelector, EnvironmentAware, BeanFactoryAware {

    private static final String[] NO_CLASS_TO_IMPORT = new String[0];

    private final Class<A> annotationType;

    protected ConfigurableEnvironment environment;

    protected ConfigurableListableBeanFactory beanFactory;

    protected BeanDefinitionRegistry registry;

    protected BeanNameGenerator beanNameGenerator;

    public EnableConfigPropertySourceLoader() {
        this.annotationType = resolveAnnotationType();
    }

    private Class<A> resolveAnnotationType() {
        ResolvableType type = ResolvableType.forType(this.getClass());
        ResolvableType superType = type.as(EnableConfigPropertySourceLoader.class);
        return (Class<A>) superType.resolveGeneric(0);
    }

    @Override
    public final String[] selectImports(AnnotationMetadata metadata) {
        AnnotationAttributes annotationAttributes = fromMap(metadata.getAnnotationAttributes(annotationType.getName()));
        EnableConfigAttributes<A> enableConfigAttributes = new EnableConfigAttributes(annotationType, annotationAttributes);
        MutablePropertySources propertySources = environment.getPropertySources();
        String propertySourceName = resolvePropertySourceName(enableConfigAttributes, metadata);
        PropertySource<?> propertySource = loadPropertySource(enableConfigAttributes, propertySourceName, metadata);
        if (enableConfigAttributes.isFirstPropertySource()) {
            propertySources.addFirst(propertySource);
        } else {
            String relativePropertySourceName = enableConfigAttributes.getAfterPropertySourceName();
            if (StringUtils.hasText(relativePropertySourceName)) {
                propertySources.addAfter(relativePropertySourceName, propertySource);
            } else {
                relativePropertySourceName = enableConfigAttributes.getBeforePropertySourceName();
            }
            if (StringUtils.hasText(relativePropertySourceName)) {
                propertySources.addBefore(relativePropertySourceName, propertySource);
            } else {
                propertySources.addLast(propertySource);
            }
        }
        return NO_CLASS_TO_IMPORT;
    }

    /**
     * Resolve the name of {@link PropertySource}
     *
     * @param enableConfigAttributes {@link EnableConfigAttributes}
     * @param metadata               {@link AnnotationMetadata}
     * @return non-null
     */
    @NonNull
    protected String resolvePropertySourceName(EnableConfigAttributes<A> enableConfigAttributes, AnnotationMetadata metadata) {
        String name = enableConfigAttributes.getName();
        if (!StringUtils.hasText(name)) {
            name = buildDefaultPropertySourceName(metadata);
        }
        return name;
    }


    /**
     * Build the default name of {@link PropertySource}
     *
     * @param metadata {@link AnnotationMetadata}
     * @return non-null
     */
    @NonNull
    protected String buildDefaultPropertySourceName(AnnotationMetadata metadata) {
        String annotationClassName = annotationType.getName();
        String introspectedClassName = metadata.getClassName();
        return introspectedClassName + "@" + annotationClassName;
    }

    /**
     * Load the {@link PropertySource}
     *
     * @param enableConfigAttributes {@link EnableConfigAttributes}
     * @param propertySourceName     the name of {@link PropertySource}
     * @param metadata               {@link AnnotationMetadata}
     * @return non-null
     */
    @NonNull
    protected abstract PropertySource<?> loadPropertySource(EnableConfigAttributes<A> enableConfigAttributes, String propertySourceName, AnnotationMetadata metadata);

    @Override
    public final void setEnvironment(Environment environment) {
        Class<ConfigurableEnvironment> targetType = ConfigurableEnvironment.class;
        Assert.isInstanceOf(targetType, environment, "The 'environment' argument must be an instance of class " + targetType.getName());
        this.environment = targetType.cast(environment);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        Class<ConfigurableListableBeanFactory> targetType = ConfigurableListableBeanFactory.class;
        Assert.isInstanceOf(targetType, beanFactory, "The 'beanFactory' argument must be an instance of class " + targetType.getName());
        Assert.isInstanceOf(BeanDefinitionRegistry.class, beanFactory, "The 'beanFactory' argument must be an instance of class " + BeanDefinitionRegistry.class.getName());
        this.beanFactory = targetType.cast(beanFactory);

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        this.beanNameGenerator = resolveAnnotatedBeanNameGenerator(registry);
        this.registry = registry;
    }

    /**
     * The annotation type
     *
     * @return
     */
    @NonNull
    public Class<A> getAnnotationType() {
        return annotationType;
    }

    @NonNull
    public ConfigurableEnvironment getEnvironment() {
        return environment;
    }

    @NonNull
    public ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }
}
