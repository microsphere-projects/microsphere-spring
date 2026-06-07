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
import io.microsphere.annotation.Nullable;
import io.microsphere.spring.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;

/**
 * The strategy interface for overriding annotation attributes.
 * <p>
 * The instance of this interface is similar to the Spring bean, which will be
 * {@link BeanUtils#initializeBean(Object, ApplicationContext) initialized} and not
 * be registered as a Spring bean into the Spring {@link BeanFactory}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see OverrideAnnotationAttributes
 * @see BeanUtils#initializeBean(Object, ApplicationContext)
 * @since 1.0.0
 */
public interface OverrideAnnotationAttributesStrategy {

    /**
     * Override annotation attributes
     *
     * @param annotationType     the annotation type
     * @param originalAttributes the original annotation attributes
     * @param annotationMetadata the annotation metadata
     * @return <code>null</code> if not override, or the overridden annotation attributes
     */
    @Nullable
    AnnotationAttributes override(@Nonnull Class<? extends Annotation> annotationType,
                                  @Nonnull AnnotationAttributes originalAttributes,
                                  @Nonnull AnnotationMetadata annotationMetadata);
}