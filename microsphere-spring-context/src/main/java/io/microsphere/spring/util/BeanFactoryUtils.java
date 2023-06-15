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
package io.microsphere.spring.util;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.spring.util.FieldUtils.getFieldValue;
import static io.microsphere.spring.util.ObjectUtils.of;
import static io.microsphere.util.ClassUtils.cast;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static org.springframework.beans.factory.BeanFactoryUtils.beanNamesForTypeIncludingAncestors;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.ObjectUtils.containsElement;
import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link BeanFactory} Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class BeanFactoryUtils {

    /**
     * Get optional Bean
     *
     * @param beanFactory {@link ListableBeanFactory}
     * @param beanName    the name of Bean
     * @param beanType    the {@link Class type} of Bean
     * @param <T>         the {@link Class type} of Bean
     * @return A bean if present , or <code>null</code>
     */
    public static <T> T getOptionalBean(ListableBeanFactory beanFactory, String beanName, Class<T> beanType) {

        if (!hasText(beanName)) {
            return null;
        }

        String[] beanNames = of(beanName);

        List<T> beans = getBeans(beanFactory, beanNames, beanType);

        return isEmpty(beans) ? null : beans.get(0);
    }


    /**
     * Gets name-matched Beans from {@link ListableBeanFactory BeanFactory}
     *
     * @param beanFactory {@link ListableBeanFactory BeanFactory}
     * @param beanNames   the names of Bean
     * @param beanType    the {@link Class type} of Bean
     * @param <T>         the {@link Class type} of Bean
     * @return the read-only and non-null {@link List} of Bean names
     */
    public static <T> List<T> getBeans(ListableBeanFactory beanFactory, String[] beanNames, Class<T> beanType) {

        if (isEmpty(beanNames)) {
            return emptyList();
        }

        // Issue : https://github.com/alibaba/spring-context-support/issues/20
        String[] allBeanNames = beanNamesForTypeIncludingAncestors(beanFactory, beanType, true, false);

        List<T> beans = new ArrayList<T>(beanNames.length);

        for (String beanName : beanNames) {
            if (containsElement(allBeanNames, beanName)) {
                beans.add(beanFactory.getBean(beanName, beanType));
            }
        }

        return unmodifiableList(beans);
    }

    /**
     * Is the given BeanFactory {@link DefaultListableBeanFactory}
     *
     * @param beanFactory {@link BeanFactory}
     * @return <code>true</code> if it's {@link DefaultListableBeanFactory}, <code>false</code> otherwise
     */
    public static boolean isDefaultListableBeanFactory(BeanFactory beanFactory) {
        return beanFactory instanceof DefaultListableBeanFactory;
    }

    public static ListableBeanFactory asListableBeanFactory(BeanFactory beanFactory) {
        return cast(beanFactory, ListableBeanFactory.class);
    }

    public static HierarchicalBeanFactory asHierarchicalBeanFactory(BeanFactory beanFactory) {
        return cast(beanFactory, HierarchicalBeanFactory.class);
    }

    public static ConfigurableBeanFactory asConfigurableBeanFactory(BeanFactory beanFactory) {
        return cast(beanFactory, ConfigurableBeanFactory.class);
    }

    public static AutowireCapableBeanFactory asAutowireCapableBeanFactory(BeanFactory beanFactory) {
        return cast(beanFactory, AutowireCapableBeanFactory.class);
    }

    public static ConfigurableListableBeanFactory asConfigurableListableBeanFactory(BeanFactory beanFactory) {
        return cast(beanFactory, ConfigurableListableBeanFactory.class);
    }

    public static DefaultListableBeanFactory asDefaultListableBeanFactory(BeanFactory beanFactory) {
        return cast(beanFactory, DefaultListableBeanFactory.class);
    }

    /**
     * Get the {@link ConfigurableListableBeanFactory#registerResolvableDependency(Class, Object) registered}
     * Resolvable Dependency Types
     *
     * @param beanFactory {@link ConfigurableListableBeanFactory}
     * @return non-null read-only {@link Set}
     */
    public static Set<Class<?>> getResolvableDependencyTypes(ConfigurableListableBeanFactory beanFactory) {
        DefaultListableBeanFactory defaultListableBeanFactory = asDefaultListableBeanFactory(beanFactory);
        if (defaultListableBeanFactory == null) {
            return emptySet();
        }
        return getResolvableDependencyTypes((DefaultListableBeanFactory) beanFactory);
    }

    /**
     * Get the {@link ConfigurableListableBeanFactory#registerResolvableDependency(Class, Object) registered}
     * Resolvable Dependency Types
     *
     * @param beanFactory {@link DefaultListableBeanFactory}
     * @return non-null read-only {@link Set}
     */
    public static Set<Class<?>> getResolvableDependencyTypes(DefaultListableBeanFactory beanFactory) {
        Map resolvableDependencies = getFieldValue(beanFactory, "resolvableDependencies", Map.class);
        return resolvableDependencies == null ? emptySet() : resolvableDependencies.keySet();
    }

}
