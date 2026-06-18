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
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.Set;

import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;

/**
 * An abstract base class for {@link ImportSelector} implementations that select imports based on
 * annotation attributes with resolved placeholders.
 * <p>
 * This class extends {@link AnnotatedBeanCapableImportCandidate} to provide common functionality
 * for handling annotated bean capabilities and implements {@link ImportSelector} to integrate
 * with Spring's import mechanism.
 *
 * <h3>Usage Example</h3>
 * Suppose you want to create an import selector for an annotation {@code @EnableMyFeature}:
 *
 * <pre>{@code
 * @Target(ElementType.TYPE)
 * @Retention(RetentionPolicy.RUNTIME)
 * @Documented
 * public @interface EnableMyFeature {
 *     String value() default "";
 * }
 *
 * public class MyFeatureImportSelector extends AnnotatedBeanCapableImportSelector<EnableMyFeature> {
 *
 *     @Override
 *     protected void selectImports(AnnotationMetadata metadata,
 *                                  ResolvablePlaceholderAnnotationAttributes<EnableMyFeature> attributes,
 *                                  Set<String> imports) {
 *         String featureName = attributes.getString("value");
 *         if (StringUtils.hasText(featureName)) {
 *             imports.add("com.example.MyFeatureConfig");
 *         }
 *     }
 * }
 * }</pre>
 *
 * @param <A> the type of the annotation to process
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotatedBeanCapableImportCandidate
 * @see ImportSelector
 * @since 1.0.0
 */
public abstract class AnnotatedBeanCapableImportSelector<A extends Annotation> extends
        AnnotatedBeanCapableImportCandidate<A> implements ImportSelector {

    @Override
    public final String[] selectImports(AnnotationMetadata metadata) {
        Set<String> imports = newLinkedHashSet();
        selectImports(metadata, getAnnotationAttributes(metadata), imports);
        return imports.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Selects the class names to be imported based on the annotation attributes.
     * <p>
     * Subclasses should override this method to add specific class names to the {@code imports} set
     * based on the resolved annotation attributes. The default implementation does nothing.
     *
     * <h3>Example Usage</h3>
     * Suppose you have an annotation {@code @EnableMyFeature(value = "com.example")}:
     * <pre>{@code
     * @Target(ElementType.TYPE)
     * @Retention(RetentionPolicy.RUNTIME)
     * @Documented
     * public @interface EnableMyFeature {
     *     String value() default "";
     * }
     *
     * public class MyFeatureImportCandidate extends AnnotatedBeanCapableImportSelector<EnableMyFeature> {
     *
     *     @Override
     *     protected void selectImports(AnnotationMetadata metadata,
     *                                  ResolvablePlaceholderAnnotationAttributes<EnableMyFeature> attributes,
     *                                  Set<String> imports) {
     *         String featureName = attributes.getString("value");
     *         if (StringUtils.hasText(featureName)) {
     *             imports.add("com.example.MyFeatureConfig");
     *         }
     *     }
     * }
     * }</pre>
     *
     * @param metadata             the {@link AnnotationMetadata} of the importing class
     * @param annotationAttributes the resolved annotation attributes with placeholders resolved
     * @param imports              the set of class names to import; add desired classes to this set
     */
    protected abstract void selectImports(AnnotationMetadata metadata,
                                          ResolvablePlaceholderAnnotationAttributes<A> annotationAttributes,
                                          Set<String> imports);
}
