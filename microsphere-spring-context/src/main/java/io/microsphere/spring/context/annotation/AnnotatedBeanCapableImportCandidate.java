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
import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;

import static io.microsphere.constants.PropertyConstants.ENABLED_PROPERTY_NAME;
import static io.microsphere.constants.SymbolConstants.AT_CHAR;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
import static org.springframework.core.ResolvableType.forType;

/**
 * An abstract base class for {@link BeanCapableImportCandidate} implementations that are driven by a specific
 * annotation type {@code A}.
 * <p>
 * This class extends {@link BeanCapableImportCandidate} to provide common bean import capabilities,
 * while adding support for processing annotation attributes with placeholder resolution.
 * Subclasses must specify the annotation type {@code A} via generics.
 *
 * <h3>Key Features</h3>
 * <ul>
 *     <li>Automatically resolves the generic annotation type {@code A} at runtime.</li>
 *     <li>Supports enabling/disabling imports via environment properties (see {@link #isEnabled(Environment, String, Class)}).</li>
 *     <li>Provides resolved annotation attributes via {@link ResolvablePlaceholderAnnotationAttributes}.</li>
 * </ul>
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * // 1. Define your custom annotation
 * @Target(ElementType.TYPE)
 * @Retention(RetentionPolicy.RUNTIME)
 * @Import(MyFeatureImportCandidate.class)
 * public @interface EnableMyFeature {
 *     String value() default "";
 * }
 *
 * // 2. Implement the Import Candidate
 * public class MyFeatureImportCandidate extends AnnotatedBeanCapableImportCandidate<EnableMyFeature> implements
 * ImportBeanDefinitionRegistrar {
 *
 *     @Override
 *     public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
 *         if (!isEnabled(importingClassMetadata)) {
 *             return;
 *         }
 *         ResolvablePlaceholderAnnotationAttributes<EnableMyFeature> attrs = getAnnotationAttributes(importingClassMetadata);
 *         String featureName = attrs.getString("value");
 *         // Register beans based on featureName...
 *     }
 * }
 *
 * // 3. Use in Configuration
 * @Configuration
 * @EnableMyFeature("my-feature")
 * public class AppConfig {
 *     // Beans will be imported if enabled
 * }
 * }</pre>
 *
 * <h3>Property-Based Control</h3>
 * The import can be controlled via properties:
 * <ul>
 *     <li>Class-specific: {@code microsphere.spring.com.example.AppConfig@com.example.EnableMyFeature.enabled=false}</li>
 *     <li>Global: {@code microsphere.spring.com.example.EnableMyFeature.enabled=false}</li>
 * </ul>
 *
 * @param <A> the type of the annotation that drives this import candidate
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanCapableImportCandidate
 * @see ResolvablePlaceholderAnnotationAttributes
 * @see AnnotatedBeanCapableImportSelector
 * @see AnnotatedBeanCapableImportBeanDefinitionRegistrar
 * @since 1.0.0
 */
public abstract class AnnotatedBeanCapableImportCandidate<A extends Annotation> extends BeanCapableImportCandidate {

    protected final Class<A> annotationType;

    public AnnotatedBeanCapableImportCandidate() {
        this.annotationType = resolveAnnotationType();
    }

    protected Class<A> resolveAnnotationType() {
        return resolveGeneric(AnnotatedBeanCapableImportCandidate.class, 0);
    }

    protected <T> Class<T> resolveGeneric(Class<?> declaredClass, int index) {
        ResolvableType type = forType(getClass());
        ResolvableType superType = type.as(declaredClass);
        return (Class<T>) superType.resolveGeneric(index);
    }

    protected boolean isEnabled(AnnotationMetadata metadata) {
        return isEnabled(getEnvironment(), metadata.getClassName(), getAnnotationType());
    }

    /**
     * Gets the annotation attributes from the given {@link AnnotationMetadata}, resolving any placeholders.
     * <p>
     * This method retrieves the attributes of the annotation type associated with this import candidate
     * from the importing class's metadata. It uses {@link ResolvablePlaceholderAnnotationAttributes} to
     * ensure that any property placeholders (e.g., {@code ${my.property}}) within the annotation attributes
     * are resolved against the Spring {@link Environment}.
     *
     * <h3>Example Usage</h3>
     * Suppose you have an annotation {@code @EnableFeature(name = "${feature.name}")} and a configuration class:
     * <pre>{@code
     * @Configuration
     * @EnableFeature(name = "${feature.name}")
     * public class MyConfig { ... }
     * }</pre>
     * If the environment property {@code feature.name} is set to {@code "MyFeature"}, calling this method
     * will return a {@link ResolvablePlaceholderAnnotationAttributes} instance where the attribute {@code name}
     * has the resolved value {@code "MyFeature"}.
     *
     * @param metadata the {@link AnnotationMetadata} of the importing class
     * @return the resolved annotation attributes, never {@code null}
     */
    @Nonnull
    protected ResolvablePlaceholderAnnotationAttributes<A> getAnnotationAttributes(AnnotationMetadata metadata) {
        return super.getAnnotationAttributes(metadata, getAnnotationType());
    }

    /**
     * Get the {@link Class annotation type}
     *
     * @return non-null
     */
    @Nonnull
    public Class<A> getAnnotationType() {
        return annotationType;
    }

    /**
     * Checks if the import candidate is enabled based on the environment properties.
     * <p>
     * The check is performed in the following order:
     * <ol>
     *     <li>Check for a class-specific property: {@code microsphere.spring.<importing-class-name>@<annotation-class-name>.enabled}</li>
     *     <li>If not found, check for a global property: {@code microsphere.spring.<annotation-class-name>.enabled}</li>
     *     <li>If neither is found, default to {@code true}</li>
     * </ol>
     *
     * <h3>Example Usage</h3>
     * Given an importing class {@code com.example.MyConfiguration} and an annotation type
     * {@code io.microsphere.spring.annotation.EnableMicrosphere}:
     * <ul>
     *     <li>If property {@code microsphere.spring.com.example.MyConfiguration@io.microsphere.spring.annotation.EnableMicrosphere.enabled=false} is set, returns {@code false}.</li>
     *     <li>If the above is not set, but {@code microsphere.spring.io.microsphere.spring.annotation.EnableMicrosphere.enabled=false} is set, returns {@code false}.</li>
     *     <li>If neither is set, returns {@code true} (default).</li>
     * </ul>
     *
     * @param environment        the Spring {@link Environment} to resolve properties from
     * @param importingClassName the name of the class importing the beans
     * @param annotationType     the annotation type associated with the import candidate
     * @return {@code true} if enabled, {@code false} otherwise
     */
    static boolean isEnabled(Environment environment, String importingClassName, Class<? extends Annotation> annotationType) {
        String propertyName = getEnabledPropertyName(importingClassName, annotationType);
        String propertyValue = environment.getProperty(propertyName);
        if (propertyValue == null) {
            propertyName = getGlobalEnabledPropertyName(annotationType);
        }
        return environment.getProperty(propertyName, boolean.class, true);
    }

    /**
     * Gets the property name that controls whether the import candidate is enabled for a specific importing class.
     * <p>
     * The property name is constructed as:
     * {@code microsphere.spring.<importing-class-name>@<annotation-class-name>.enabled}
     * <h3>Example Usage</h3>
     * If the importing class is {@code com.example.MyConfiguration} and the annotation type is
     * {@code io.microsphere.spring.annotation.EnableMicrosphere},
     * the returned property name will be:
     * {@code microsphere.spring.com.example.MyConfiguration@io.microsphere.spring.annotation.EnableMicrosphere.enabled}
     *
     * @param importingClassName the name of the importing class
     * @param annotationType     the annotation type
     * @return the enabled property name
     */
    public static String getEnabledPropertyName(String importingClassName, Class<? extends Annotation> annotationType) {
        return MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + importingClassName + AT_CHAR +
                annotationType.getName() + DOT_CHAR + ENABLED_PROPERTY_NAME;
    }

    /**
     * Gets the global property name that controls whether the import candidate is enabled for the specified annotation type.
     * <p>
     * The property name is constructed as:
     * {@code microsphere.spring.<annotation-class-name>.enabled}
     * <h3>Example Usage</h3>
     * If the annotation type is {@code io.microsphere.spring.annotation.EnableMicrosphere},
     * the returned property name will be:
     * {@code microsphere.spring.io.microsphere.spring.annotation.EnableMicrosphere.enabled}
     *
     * @param annotationType the annotation type
     * @return the global enabled property name
     */
    public static String getGlobalEnabledPropertyName(Class<? extends Annotation> annotationType) {
        return MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + annotationType.getName() + DOT_CHAR + ENABLED_PROPERTY_NAME;
    }
}