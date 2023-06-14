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

import io.microsphere.spring.beans.factory.Dependency;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Set;

import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgumentClass;

/**
 * The Annotated Injection {@link Dependency} Resolver for Spring Beans
 *
 * @param <A> the type of {@link Annotation} for injection
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface AnnotatedInjectionDependencyResolver<A extends Annotation> {

    /**
     * Get the type of {@link Annotation} for injection
     *
     * @return non-null
     */
    default Class<A> getAnnotationType() {
        return resolveActualTypeArgumentClass(getClass(), AnnotatedInjectionDependencyResolver.class, 0);
    }

    /**
     * Get the injection annotation from the annotated element
     *
     * @param field {@link Field}
     * @return the injection annotation if found
     */
    default A getAnnotation(Field field) {
        return getAnnotation((AnnotatedElement) field);
    }

    /**
     * Get the injection annotation from the annotated element
     *
     * @param parameter {@link Parameter}
     * @return the injection annotation if found
     */
    default A getAnnotation(Parameter parameter) {
        return getAnnotation((AnnotatedElement) parameter);
    }

    /**
     * Get the injection annotation from the annotated element
     *
     * @param annotated {@link Field} or {@link Parameter}
     * @return the injection annotation if found
     */
    default A getAnnotation(AnnotatedElement annotated) {
        return annotated.getAnnotation(getAnnotationType());
    }

    /**
     * Resolve the bean names as the dependencies from the specified {@link Field field}
     *  @param field              the {@link Field field} was annotated by the annotation
     * @param beanFactory        {@link ConfigurableListableBeanFactory}
     * @param dependentBeanNames the dependent bean names to be manipulated
     */
    void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames);

    /**
     * Resolve the bean names as the dependencies from the specified {@link Parameter parameter}
     *
     * @param parameter          the specified {@link Parameter parameter} of a method or constructor
     *                           was annotated by the annotation
     * @param beanFactory        {@link ConfigurableListableBeanFactory}
     * @param dependentBeanNames the dependent bean names to be manipulated
     * @return non-null read-only {@link List}
     */
    void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames);
}
