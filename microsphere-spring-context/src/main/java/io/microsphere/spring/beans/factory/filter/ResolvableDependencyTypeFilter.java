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
package io.microsphere.spring.beans.factory.filter;

import io.microsphere.filter.Filter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Set;

import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asDefaultListableBeanFactory;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.getResolvableDependencyTypes;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerSingleton;

/**
 * A {@link Filter} implementation that evaluates whether a given class is a resolvable dependency type.
 * <p>
 * This class is used to determine if a specified class can be resolved as a dependency by checking it against a set of known resolvable dependency types.
 * It is particularly useful in Spring contexts where certain types need to be filtered based on their resolvability.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * ConfigurableListableBeanFactory beanFactory = ...; // Obtain from Spring context
 * ResolvableDependencyTypeFilter filter = new ResolvableDependencyTypeFilter(beanFactory);
 *
 * Class<?> dependencyType = MyService.class;
 * boolean isResolvable = filter.accept(dependencyType);  // Returns true if MyService is a resolvable dependency
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ResolvableDependencyTypeFilter implements Filter<Class<?>> {

    public static final String BEAN_NAME = "resolvableDependencyTypeFilter";

    private final Set<Class<?>> resolvableDependencyTypes;

    /**
     * Constructs a new {@link ResolvableDependencyTypeFilter} from a {@link ConfigurableListableBeanFactory}.
     * The factory is converted to a {@link DefaultListableBeanFactory} internally, and the filter
     * registers itself as a singleton bean in the factory.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
     *   ResolvableDependencyTypeFilter filter = new ResolvableDependencyTypeFilter(beanFactory);
     * }</pre>
     *
     * @param beanFactory the {@link ConfigurableListableBeanFactory} to resolve dependency types from
     */
    public ResolvableDependencyTypeFilter(ConfigurableListableBeanFactory beanFactory) {
        this(asDefaultListableBeanFactory(beanFactory));
    }

    /**
     * Constructs a new {@link ResolvableDependencyTypeFilter} from a {@link DefaultListableBeanFactory}.
     * Retrieves the set of resolvable dependency types from the given factory and registers
     * this filter as a singleton bean under the name {@link #BEAN_NAME}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
     *   ResolvableDependencyTypeFilter filter = new ResolvableDependencyTypeFilter(beanFactory);
     * }</pre>
     *
     * @param beanFactory the {@link DefaultListableBeanFactory} to resolve dependency types from
     */
    public ResolvableDependencyTypeFilter(DefaultListableBeanFactory beanFactory) {
        this.resolvableDependencyTypes = getResolvableDependencyTypes(beanFactory);
        register(beanFactory);
    }

    private void register(SingletonBeanRegistry registry) {
        registerSingleton(registry, BEAN_NAME, this);
    }

    /**
     * Retrieves the existing {@link ResolvableDependencyTypeFilter} singleton from the given
     * {@link BeanFactory}, or creates and registers a new one if none exists.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurableApplicationContext context = ...;
     *   ResolvableDependencyTypeFilter filter = ResolvableDependencyTypeFilter.get(context.getBeanFactory());
     * }</pre>
     *
     * @param beanFactory the {@link BeanFactory} to look up or register the filter in
     * @return the existing or newly created {@link ResolvableDependencyTypeFilter} instance
     */
    public static ResolvableDependencyTypeFilter get(BeanFactory beanFactory) {
        return get(asDefaultListableBeanFactory(beanFactory));
    }

    static ResolvableDependencyTypeFilter get(DefaultListableBeanFactory beanFactory) {
        ResolvableDependencyTypeFilter filter = (ResolvableDependencyTypeFilter) beanFactory.getSingleton(BEAN_NAME);
        return filter == null ? new ResolvableDependencyTypeFilter(beanFactory) : filter;
    }

    /**
     * Determines whether the given class is assignable to any of the known resolvable dependency types.
     * Returns {@code true} if the class matches a resolvable dependency type, {@code false} otherwise.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ResolvableDependencyTypeFilter filter = ResolvableDependencyTypeFilter.get(beanFactory);
     *   assertTrue(filter.accept(BeanFactory.class));
     *   assertTrue(filter.accept(ApplicationContext.class));
     *   assertFalse(filter.accept(MyCustomClass.class));
     * }</pre>
     *
     * @param classToFilter the class to evaluate against resolvable dependency types
     * @return {@code true} if the class is assignable to a resolvable dependency type, {@code false} otherwise
     * @throws NullPointerException if {@code classToFilter} is {@code null}
     */
    @Override
    public boolean accept(Class<?> classToFilter) {
        boolean filtered = false;
        for (Class<?> resolvableDependencyType : resolvableDependencyTypes) {
            if (resolvableDependencyType.isAssignableFrom(classToFilter)) {
                filtered = true;
                break;
            }
        }
        return filtered;
    }

    /**
     * Returns a string representation of this filter, including the set of resolvable dependency types.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ResolvableDependencyTypeFilter filter = ResolvableDependencyTypeFilter.get(beanFactory);
     *   String description = filter.toString();
     *   // e.g. "ResolvableDependencyTypeFilter{resolvableDependencyTypes=[interface org.springframework.beans.factory.BeanFactory, ...]}"
     * }</pre>
     *
     * @return a string describing this filter and its resolvable dependency types
     */
    @Override
    public String toString() {
        return "ResolvableDependencyTypeFilter{" +
                "resolvableDependencyTypes=" + resolvableDependencyTypes +
                '}';
    }
}
