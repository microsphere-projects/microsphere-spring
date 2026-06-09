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

import io.microsphere.annotation.Nonnull;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The meta-annotation that indicates the attributes of the annotation should be overridden.
 * <p>
 * The annotation must {@link Import @Import} an {@link ImportBeanDefinitionRegistrar} or {@link ImportSelector}
 * implementation that must extend the abstract class {@link BeanCapableImportCandidate} or it subtype.
 * <p>
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Define a custom annotation with an override strategy
 * @OverrideAnnotationAttributes(strategy = MyCustomStrategy.class)
 * @Import(MyImportRegistrar.class)
 * public @interface MyCustomImport {
 *     String value() default "";
 * }
 *
 * // In your ImportSelector or ImportBeanDefinitionRegistrar implementation:
 * public class MyImportRegistrar extends BeanCapableImportCandidate implements ImportBeanDefinitionRegistrar {
 *     public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
 *          // ...
 *     }
 * }
 *
 * // Define an override strategy
 * public class MyCustomStrategy implements OverrideAnnotationAttributesStrategy {
 *     @Override
 *     public AnnotationAttributes override(AnnotationAttributes originalAttributes, Class<? extends Annotation> annotationType, AnnotationMetadata metadata) {
 *         // Custom logic to modify attributes
 *         return originalAttributes;
 *     }
 * }
 *
 * @MyCustomImport
 * public class AppConfig { }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see OverrideAnnotationAttributesStrategy
 * @see ConfigurationPropertyOverrideAnnotationAttributesStrategy
 * @see BeanCapableImportCandidate#getOverriddenAnnotationAttributes
 * @see Import
 * @since 1.0.0
 */
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
public @interface OverrideAnnotationAttributes {

    /**
     * The strategy class for overriding annotation attributes.
     *
     * @return {@link ConfigurationPropertyOverrideAnnotationAttributesStrategy} as default, the implementation class
     * must be a concrete class and have a default constructor.
     * @see ConfigurationPropertyOverrideAnnotationAttributesStrategy
     */
    @Nonnull
    Class<? extends OverrideAnnotationAttributesStrategy> strategy() default ConfigurationPropertyOverrideAnnotationAttributesStrategy.class;
}
