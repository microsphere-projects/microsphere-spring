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
package io.microsphere.spring.beans.factory;

import io.microsphere.spring.beans.factory.annotation.AnnotatedInjectionPointDependencyResolver;
import io.microsphere.spring.beans.factory.annotation.AutowiredInjectionPointDependencyResolver;
import io.microsphere.spring.beans.factory.annotation.ResourceInjectionPointDependencyResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Set;

/**
 * Resolver for injection point dependencies in Spring-managed beans.
 *
 * <p>This interface provides methods to resolve bean names that represent dependencies based on various
 * injection points, such as fields, methods (e.g., setters), constructors, and parameters annotated with
 * dependency injection annotations.</p>
 *
 * <h3>Example Usage</h3>
 *
 * <pre>{@code
 * public class MyDependencyResolver implements InjectionPointDependencyResolver {
 *
 *     @Override
 *     public void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
 *         // Resolve dependency from a field
 *     }
 *
 *     @Override
 *     public void resolve(Method method, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
 *         // Resolve dependency from a method (e.g., setter)
 *     }
 *
 *     @Override
 *     public void resolve(Constructor constructor, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
 *         // Resolve dependency from a constructor
 *     }
 *
 *     @Override
 *     public void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
 *         // Resolve dependency from a parameter annotated with @Autowired or similar
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractInjectionPointDependencyResolver
 * @see AnnotatedInjectionPointDependencyResolver
 * @see BeanMethodInjectionPointDependencyResolver
 * @see ConstructionInjectionPointDependencyResolver
 * @see AutowiredInjectionPointDependencyResolver
 * @see ResourceInjectionPointDependencyResolver
 * @since 1.0.0
 */
public interface InjectionPointDependencyResolver {

    /**
     * Resolve the bean names as the dependencies from the specified {@link Field field}
     *
     * @param field              the {@link Field field} may be an injection point
     * @param beanFactory        {@link ConfigurableListableBeanFactory}
     * @param dependentBeanNames the dependent bean names to be manipulated
     */
    void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames);

    /**
     * Resolve the bean names as the dependencies from the specified {@link Method method}
     *
     * @param method             the {@link Method method} may be an injection point
     * @param beanFactory        {@link ConfigurableListableBeanFactory}
     * @param dependentBeanNames the dependent bean names to be manipulated
     */
    void resolve(Method method, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames);

    /**
     * Resolve the bean names as the dependencies from the specified {@link Constructor constructor}
     *
     * @param constructor        the {@link Constructor constructor} may be an injection point
     * @param beanFactory        {@link ConfigurableListableBeanFactory}
     * @param dependentBeanNames the dependent bean names to be manipulated
     */
    void resolve(Constructor constructor, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames);

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
