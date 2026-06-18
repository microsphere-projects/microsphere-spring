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

package io.microsphere.spring.context.annotation;

import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.Set;

import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator.INSTANCE;

/**
 * An abstract base class for {@link ImportBeanDefinitionRegistrar} implementations that are driven by a specific
 * annotation type {@code A}.
 * <p>
 * This class extends {@link AnnotatedBeanCapableImportCandidate} to provide annotation-driven import capabilities
 * and implements {@link ImportBeanDefinitionRegistrar} to handle bean definition registration.
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * public class MyFeatureRegistrar extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableMyFeature> {
 *
 *     @Override
 *     protected void registerBeanDefinitions(AnnotationMetadata metadata,
 *                                            BeanDefinitionRegistry registry,
 *                                            BeanNameGenerator importBeanNameGenerator,
 *                                            ResolvablePlaceholderAnnotationAttributes<EnableMyFeature> attributes) {
 *         String featureName = attributes.getString("value");
 *         if (StringUtils.hasText(featureName)) {
 *             GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
 *             beanDefinition.setBeanClass(MyFeatureService.class);
 *             beanDefinition.getPropertyValues().add("name", featureName);
 *             registry.registerBeanDefinition("myFeatureService", beanDefinition);
 *         }
 *     }
 * }
 * }</pre>
 *
 * @param <A> the annotation type that drives the import candidate selection and bean definition registration
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotatedBeanCapableImportCandidate
 * @see BeanCapableImportCandidate
 * @see ImportBeanDefinitionRegistrar
 * @since 1.0.0
 */
public abstract class AnnotatedBeanCapableImportBeanDefinitionRegistrar<A extends Annotation> extends
        AnnotatedBeanCapableImportCandidate<A> implements ImportBeanDefinitionRegistrar {

    @Override
    public final void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                              BeanNameGenerator importBeanNameGenerator) {
        if (isEnabled(metadata)) {
            Set<String> imports = newLinkedHashSet();
            ResolvablePlaceholderAnnotationAttributes<A> annotationAttributes = getAnnotationAttributes(metadata);
            registerBeanDefinitions(metadata, registry, importBeanNameGenerator, annotationAttributes);
        }
    }

    @Override
    public final void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        // This method should not be invoked since Spring 5.2, but it prevents the subclasses to implement.
        registerBeanDefinitions(metadata, registry, INSTANCE);
    }

    /**
     * Registers bean definitions based on the annotation attributes.
     * <p>
     * Subclasses should override this method to register specific bean definitions
     * into the {@link BeanDefinitionRegistry} based on the resolved annotation attributes.
     * The default implementation does nothing.
     *
     * <h3>Example Usage</h3>
     * Suppose you have an annotation {@code @EnableService(prefix = "${service.prefix}")}:
     * <pre>{@code
     * @Override
     * protected void registerBeanDefinitions(AnnotationMetadata metadata,
     *                                        BeanDefinitionRegistry registry,
     *                                        BeanNameGenerator importBeanNameGenerator,
     *                                        ResolvablePlaceholderAnnotationAttributes<EnableService> attributes) {
     *     String prefix = attributes.getString("prefix");
     *     if (StringUtils.hasText(prefix)) {
     *         GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
     *         beanDefinition.setBeanClass(MyService.class);
     *         beanDefinition.getPropertyValues().add("prefix", prefix);
     *         registry.registerBeanDefinition("myService", beanDefinition);
     *     }
     * }
     * }</pre>
     *
     * @param metadata                the {@link AnnotationMetadata} of the importing class
     * @param registry                the {@link BeanDefinitionRegistry} to register bean definitions into
     * @param importBeanNameGenerator the {@link BeanNameGenerator} to use for generating bean names
     * @param annotationAttributes    the resolved annotation attributes with placeholders resolved
     */
    protected abstract void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                                    BeanNameGenerator importBeanNameGenerator,
                                                    ResolvablePlaceholderAnnotationAttributes<A> annotationAttributes);
}