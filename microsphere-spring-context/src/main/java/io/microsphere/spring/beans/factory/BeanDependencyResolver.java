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

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Set;

/**
 * The interface to resolve the dependencies
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RootBeanDefinition
 * @see ConfigurableListableBeanFactory
 * @see DefaultListableBeanFactory
 * @since 1.0.0
 */
public interface BeanDependencyResolver {

    /**
     * Resolve all beans with their dependent bean names from the given {@link ConfigurableListableBeanFactory BeanFactory}
     *
     * @param beanFactory {@link ConfigurableListableBeanFactory}
     * @return non-null read-only {@link Map}
     */
    @NonNull
    Map<String, Set<String>> resolve(ConfigurableListableBeanFactory beanFactory);

    /**
     * Resolve the bean names as the dependencies from the given {@link RootBeanDefinition merged bean definition}
     *
     * @param beanName             the bean name
     * @param mergedBeanDefinition the {@link RootBeanDefinition merged BeanDefinition}
     * @param beanFactory          {@link ConfigurableListableBeanFactory}
     * @return non-null read-only {@link Set}
     */
    @NonNull
    Set<String> resolve(String beanName, RootBeanDefinition mergedBeanDefinition, ConfigurableListableBeanFactory beanFactory);
}
