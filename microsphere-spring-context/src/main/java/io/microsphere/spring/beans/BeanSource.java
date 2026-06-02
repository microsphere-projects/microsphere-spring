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

package io.microsphere.spring.beans;

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.spring.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.ServiceLoader;
import java.util.Set;

import static io.microsphere.spring.core.io.support.SpringFactoriesLoaderUtils.loadFactoryClasses;
import static io.microsphere.util.ServiceLoaderUtils.getServiceClasses;

/**
 * The enumeration of Bean Sources
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanFactory
 * @see SpringFactoriesLoader
 * @see ServiceLoader
 * @since 1.0.0
 */
public enum BeanSource {

    /**
     * Bean from {@link BeanFactory Bean Factory}.
     *
     * @see BeanFactory
     * @see ConfigurableListableBeanFactory
     */
    BEAN_FACTORY {
        @Override
        public <T> Set<Class<T>> getBeanTypes(ConfigurableListableBeanFactory beanFactory, Class<T> beanType) {
            return BeanFactoryUtils.getBeanTypes(beanFactory, beanType, true, false);
        }
    },

    /**
     * Bean from {@link SpringFactoriesLoader Spring Factories},
     * the given type from {@value SpringFactoriesLoader#FACTORIES_RESOURCE_LOCATION} files.
     *
     * @see SpringFactoriesLoader
     */
    SPRING_FACTORIES {
        @Override
        public <T> Set<Class<T>> getBeanTypes(ConfigurableListableBeanFactory beanFactory, Class<T> beanType) {
            return loadFactoryClasses(beanType, beanFactory.getBeanClassLoader());
        }
    },

    /**
     * Bean from {@link ServiceLoader Java Service Provider},
     * the given type from {@code META-INF/services} files.
     *
     * @see ServiceLoader
     */
    JAVA_SERVICE_PROVIDER {
        @Override
        public <T> Set<Class<T>> getBeanTypes(ConfigurableListableBeanFactory beanFactory, Class<T> beanType) {
            return getServiceClasses(beanType, beanFactory.getBeanClassLoader());
        }
    };

    /**
     * Get the all bean types from the current {@link BeanSource} by given base bean type
     *
     * @param beanFactory the {@link ConfigurableListableBeanFactory}
     * @param beanType    the bean type
     * @return the all bean types from the current {@link BeanSource} by given base
     */
    @Nonnull
    @Immutable
    public abstract <T> Set<Class<T>> getBeanTypes(ConfigurableListableBeanFactory beanFactory, Class<T> beanType);

}