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
package io.microsphere.spring.beans.factory.annotation;

import io.microsphere.spring.beans.factory.DependencyInjectionResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgumentClass;

/**
 * Abstract Annotated {@link DependencyInjectionResolver}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class AnnotatedDependencyInjectionResolver<A extends Annotation> extends AbstractDependencyInjectionResolver {

    private final Class<A> annotationType;

    public AnnotatedDependencyInjectionResolver() {
        this.annotationType = resolveActualTypeArgumentClass(getClass(), AnnotatedDependencyInjectionResolver.class, 0);
    }

    public AnnotatedDependencyInjectionResolver(Class<A> annotationType) {
        this.annotationType = annotationType;
    }

    public final Class<A> getAnnotationType() {
        return annotationType;
    }

    /**
     * Get the injection annotation from the annotated element
     *
     * @param field {@link Field}
     * @return the injection annotation if found
     */
    protected A getAnnotation(Field field) {
        return getAnnotation((AnnotatedElement) field);
    }

    /**
     * Get the injection annotation from the annotated element
     *
     * @param parameter {@link Parameter}
     * @return the injection annotation if found
     */
    protected A getAnnotation(Parameter parameter) {
        return getAnnotation((AnnotatedElement) parameter);
    }

    /**
     * Get the injection annotation from the annotated element
     *
     * @param annotated {@link Field} or {@link Parameter}
     * @return the injection annotation if found
     */
    protected A getAnnotation(AnnotatedElement annotated) {
        return annotated.getAnnotation(getAnnotationType());
    }

}