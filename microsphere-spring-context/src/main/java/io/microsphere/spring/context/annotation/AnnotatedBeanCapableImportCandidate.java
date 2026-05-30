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
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;

import static org.springframework.core.ResolvableType.forType;

/**
 * The extension of {@link BeanCapableImportCandidate} for Annotated {@link Configuration} class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanCapableImportCandidate
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

    /**
     * Get the {@link ResolvablePlaceholderAnnotationAttributes}
     *
     * @param metadata {@link AnnotationMetadata}
     * @return non-null
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
}