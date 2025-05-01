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
 * A class to filter {@link ConfigurableListableBeanFactory#registerResolvableDependency(Class, Object) Resolvable Dependency Type}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ResolvableDependencyTypeFilter implements Filter<Class<?>> {

    public static final String BEAN_NAME = "resolvableDependencyTypeFilter";

    private final Set<Class<?>> resolvableDependencyTypes;

    public ResolvableDependencyTypeFilter(ConfigurableListableBeanFactory beanFactory) {
        this(asDefaultListableBeanFactory(beanFactory));
    }

    public ResolvableDependencyTypeFilter(DefaultListableBeanFactory beanFactory) {
        this.resolvableDependencyTypes = getResolvableDependencyTypes(beanFactory);
        register(beanFactory);
    }

    private void register(SingletonBeanRegistry registry) {
        registerSingleton(registry, BEAN_NAME, this);
    }

    public static ResolvableDependencyTypeFilter get(BeanFactory beanFactory) {
        return get(asDefaultListableBeanFactory(beanFactory));
    }

    static ResolvableDependencyTypeFilter get(DefaultListableBeanFactory beanFactory) {
        ResolvableDependencyTypeFilter filter = (ResolvableDependencyTypeFilter) beanFactory.getSingleton(BEAN_NAME);
        return filter == null ? new ResolvableDependencyTypeFilter(beanFactory) : filter;
    }

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

    @Override
    public String toString() {
        return "ResolvableDependencyTypeFilter{" +
                "resolvableDependencyTypes=" + resolvableDependencyTypes +
                '}';
    }
}
