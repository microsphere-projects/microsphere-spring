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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;

import static io.microsphere.spring.core.io.support.SpringFactoriesLoaderUtils.loadFactories;

/**
 * A composite implementation of the {@link InjectionPointDependencyResolver} interface that delegates to
 * multiple resolvers. It combines all available resolvers in the given {@link Iterable} and applies them one by one
 * when resolving dependencies for various injection points such as fields, methods, constructors, and parameters.
 *
 * <p><strong>Example:</strong> Suppose you have two custom resolvers: 
 * {@code CustomFieldResolver} and {@code CustomMethodResolver}.
 * You can compose them using this class as follows:</p>
 *
 * <pre>
 * Iterable<InjectionPointDependencyResolver> resolvers = Arrays.asList(
 *     new CustomFieldResolver(), 
 *     new CustomMethodResolver()
 * );
 * InjectionPointDependencyResolver resolver = new InjectionPointDependencyResolvers(resolvers);
 * </pre>
 *
 * <p>You can then use the resolver to resolve dependencies at different injection points:</p>
 *
 * <ul>
 *   <li>{@link #resolve(Field, ConfigurableListableBeanFactory, Set)}</li>
 *   <li>{@link #resolve(Method, ConfigurableListableBeanFactory, Set)}</li>
 *   <li>{@link #resolve(Constructor, ConfigurableListableBeanFactory, Set)}</li>
 *   <li>{@link #resolve(Parameter, ConfigurableListableBeanFactory, Set)}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class InjectionPointDependencyResolvers implements InjectionPointDependencyResolver {

    private final Iterable<InjectionPointDependencyResolver> resolvers;

    public InjectionPointDependencyResolvers(BeanFactory beanFactory) {
        this(loadFactories(beanFactory, InjectionPointDependencyResolver.class));
    }

    public InjectionPointDependencyResolvers(Iterable<InjectionPointDependencyResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        resolvers.forEach(resolver -> resolver.resolve(field, beanFactory, dependentBeanNames));
    }

    @Override
    public void resolve(Method method, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        resolvers.forEach(resolver -> resolver.resolve(method, beanFactory, dependentBeanNames));
    }

    @Override
    public void resolve(Constructor constructor, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        resolvers.forEach(resolver -> resolver.resolve(constructor, beanFactory, dependentBeanNames));
    }

    public void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        resolvers.forEach(resolver -> resolver.resolve(parameter, beanFactory, dependentBeanNames));
    }
}
