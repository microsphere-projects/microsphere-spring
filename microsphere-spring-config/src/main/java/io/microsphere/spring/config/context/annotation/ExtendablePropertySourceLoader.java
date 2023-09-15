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

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * Abstract {@link ImportSelector} class to load the {@link PropertySource PropertySource}
 * when the {@link Configuration configuration} annotated the Enable annotation that meta-annotates {@link PropertySourceExtension @PropertySourceExtension}
 *
 * @param <A>  The type of {@link Annotation} must meta-annotate {@link PropertySourceExtension}
 * @param <EA> The {@link PropertySourceExtensionAttributes} or its subtype
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourceExtension
 * @see PropertySourceExtensionAttributes
 * @see ImportSelector
 * @since 1.0.0
 */
public abstract class ExtendablePropertySourceLoader<A extends Annotation, EA extends PropertySourceExtensionAttributes<A>>
        extends AnnotatedPropertySourceLoader<A> {

    private final Class<EA> extensionAttributesType;

    public ExtendablePropertySourceLoader() {
        super();
        this.extensionAttributesType = resolveExtensionAttributesType();
    }

    @Override
    protected Class<A> resolveAnnotationType() {
        Class<A> annotationType = super.resolveAnnotationType();
        if (Annotation.class.equals(annotationType)) {
            annotationType = (Class<A>) PropertySourceExtension.class;
        }
        return annotationType;
    }

    protected Class<EA> resolveExtensionAttributesType() {
        ResolvableType type = ResolvableType.forType(this.getClass());
        ResolvableType superType = type.as(ExtendablePropertySourceLoader.class);
        return (Class<EA>) superType.resolveGeneric(1);
    }

    /**
     * Get the type of {@link EA}
     *
     * @return non-null
     * @see PropertySourceExtensionAttributes
     */
    public final Class<EA> getExtensionAttributesType() {
        return extensionAttributesType;
    }

    @Override
    protected final void loadPropertySource(AnnotationAttributes attributes, AnnotationMetadata metadata,
                                            String propertySourceName, MutablePropertySources propertySources) throws Throwable {
        Class<A> annotationType = getAnnotationType();
        Class<EA> extensionAttributesClass = getExtensionAttributesType();
        ConfigurableEnvironment environment = getEnvironment();
        EA extensionAttributes = buildExtensionAttributes(annotationType, extensionAttributesClass, attributes, environment);
        PropertySource<?> propertySource = loadPropertySource(extensionAttributes, propertySourceName, metadata);
        if (propertySource == null) {
            logger.warn("The PropertySource[annotationType : '{}' , configuration : '{}'] can't be loaded", annotationType.getName(), metadata.getClassName());
        } else {
            if (extensionAttributes.isFirstPropertySource()) {
                propertySources.addFirst(propertySource);
            } else {
                String relativePropertySourceName = extensionAttributes.getAfterPropertySourceName();
                if (StringUtils.hasText(relativePropertySourceName)) {
                    propertySources.addAfter(relativePropertySourceName, propertySource);
                } else {
                    relativePropertySourceName = extensionAttributes.getBeforePropertySourceName();
                }
                if (StringUtils.hasText(relativePropertySourceName)) {
                    propertySources.addBefore(relativePropertySourceName, propertySource);
                } else {
                    propertySources.addLast(propertySource);
                }
            }
        }
    }

    /**
     * Build an instance of {@link EA}
     *
     * @param annotationType          the type of {@link A}
     * @param extensionAttributesType the type of {@link EA}
     * @param attributes              {@link AnnotationAttributes}
     * @param environment             {@link ConfigurableEnvironment}
     * @return non-null
     * @throws Throwable
     */
    protected EA buildExtensionAttributes(Class<A> annotationType, Class<EA> extensionAttributesType,
                                          AnnotationAttributes attributes, ConfigurableEnvironment environment) throws Throwable {
        Constructor constructor = extensionAttributesType.getConstructor(Map.class, Class.class, PropertyResolver.class);
        return (EA) constructor.newInstance(attributes, annotationType, environment);
    }

    /**
     * Load the {@link PropertySource}
     *
     * @param extensionAttributes {@link EA}
     * @param propertySourceName  the name of {@link PropertySource}
     * @param metadata            {@link AnnotationMetadata}
     * @return <code>null</code> if the {@link PropertySource} can't be loaded
     * @throws Throwable the failure of the loading
     */
    @Nullable
    protected abstract PropertySource<?> loadPropertySource(EA extensionAttributes,
                                                            String propertySourceName, AnnotationMetadata metadata) throws Throwable;

}
