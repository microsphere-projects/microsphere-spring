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

import io.microsphere.annotation.Nonnull;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.Map;
import java.util.Set;

/**
 * A strategy interface for resolving bean dependencies within a Spring {@link ConfigurableListableBeanFactory}.
 * Implementations of this interface should provide logic to analyze and extract bean dependencies,
 * particularly based on the configuration metadata encapsulated in a {@link RootBeanDefinition}.
 *
 * <p>
 * This interface is useful in scenarios where advanced visibility into the dependency relationships
 * between beans is required, such as in dependency graph analysis, debugging tools, or specialized
 * container diagnostics.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>
 * // Assume a custom implementation of BeanDependencyResolver is created
 * BeanDependencyResolver resolver = new CustomDependencyResolver();
 *
 * // Resolve all beans and their dependencies in the bean factory
 * Map<String, Set<String>> allDependencies = resolver.resolve(beanFactory);
 *
 * // Resolve dependencies for a specific bean
 * Set<String> dependencies = resolver.resolve("myBeanName", mergedBeanDefinition, beanFactory);
 * </pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see #resolve(ConfigurableListableBeanFactory)
 * @see #resolve(String, RootBeanDefinition, ConfigurableListableBeanFactory)
 * @see DefaultBeanDependencyResolver
 * @see RootBeanDefinition
 * @see ConfigurableListableBeanFactory
 * @since 1.0.0
 */
public interface BeanDependencyResolver {

    /**
     * Resolve all beans with their dependent bean names from the given {@link ConfigurableListableBeanFactory BeanFactory}
     *
     * @param beanFactory {@link ConfigurableListableBeanFactory}
     * @return non-null read-only {@link Map}
     */
    @Nonnull
    Map<String, Set<String>> resolve(ConfigurableListableBeanFactory beanFactory);

    /**
     * Resolve the bean names as the dependencies from the given {@link RootBeanDefinition merged bean definition}
     *
     * @param beanName             the bean name
     * @param mergedBeanDefinition the {@link RootBeanDefinition merged BeanDefinition}
     * @param beanFactory          {@link ConfigurableListableBeanFactory}
     * @return non-null read-only {@link Set}
     */
    @Nonnull
    Set<String> resolve(String beanName, RootBeanDefinition mergedBeanDefinition, ConfigurableListableBeanFactory beanFactory);
}
