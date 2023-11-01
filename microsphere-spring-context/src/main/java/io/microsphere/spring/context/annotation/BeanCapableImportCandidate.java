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
package io.microsphere.spring.context.annotation;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import static io.microsphere.text.FormatUtils.format;

/**
 * The {@link Import @Import} candidate is an instance of {@link ImportSelector} or {@link ImportBeanDefinitionRegistrar}
 * and not a regular Spring bean, which only invokes {@link BeanClassLoaderAware}, {@link BeanFactoryAware},
 * {@link EnvironmentAware}, and {@link ResourceLoaderAware} contracts in order if they are implemented, thus it will
 * not be  {@link AbstractAutowireCapableBeanFactory#populateBean(String, RootBeanDefinition, BeanWrapper) populated} and
 * {@link AbstractAutowireCapableBeanFactory#initializeBean(Object, String) initialized} .
 * <p>
 * The current abstract implementation is a template class supports the population and initialization,
 * the sub-class must implement the interface {@link ImportSelector} or {@link ImportBeanDefinitionRegistrar}, and can't
 * override those methods:
 * <ul>
 *     <li>{@link #setBeanClassLoader(ClassLoader)}</li>
 *     <li>{@link #setBeanFactory(BeanFactory)}</li>
 *     <li>{@link #setEnvironment(Environment)}</li>
 *     <li>{@link #setResourceLoader(ResourceLoader)}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractAutowireCapableBeanFactory#populateBean(String, RootBeanDefinition, BeanWrapper)
 * @see AbstractAutowireCapableBeanFactory#initializeBean(Object, String)
 * @see org.springframework.context.support.ApplicationContextAwareProcessor
 * @since 1.0.0
 */
public abstract class BeanCapableImportCandidate implements BeanClassLoaderAware,
        BeanFactoryAware, EnvironmentAware, ResourceLoaderAware {

    protected ClassLoader classLoader;

    protected ConfigurableListableBeanFactory beanFactory;

    protected ConfigurableEnvironment environment;

    protected ResourceLoader resourceLoader;

    @Override
    public final void setBeanClassLoader(ClassLoader classLoader) {
        if (this.classLoader == null) {
            this.classLoader = classLoader;
            assertImportCandidate();
        }
    }


    @Override
    public final void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (this.beanFactory == null) {
            Class<ConfigurableListableBeanFactory> targetType = ConfigurableListableBeanFactory.class;
            Assert.isInstanceOf(targetType, beanFactory, "The 'beanFactory' argument must be an instance of class " + targetType.getName());
            this.beanFactory = targetType.cast(beanFactory);
        }
    }

    @Override
    public final void setEnvironment(Environment environment) {
        if (this.environment == null) {
            Class<ConfigurableEnvironment> targetType = ConfigurableEnvironment.class;
            Assert.isInstanceOf(targetType, environment, "The 'environment' argument must be an instance of class " + targetType.getName());
            this.environment = targetType.cast(environment);
        }
    }

    @Override
    public final void setResourceLoader(ResourceLoader resourceLoader) {
        if (this.resourceLoader == null) {
            this.resourceLoader = resourceLoader;
            initializeSelf();
        }
    }

    /**
     * Get the {@link ClassLoader} instance
     *
     * @return non-null
     */
    @NonNull
    public final ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * The {@link ConfigurableListableBeanFactory} instance
     *
     * @return non-null
     */
    @NonNull
    public final ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }


    /**
     * The {@link ConfigurableEnvironment} instance
     *
     * @return non-null
     */
    @NonNull
    public final ConfigurableEnvironment getEnvironment() {
        return environment;
    }

    /**
     * The {@link ResourceLoader} instance
     *
     * @return non-null
     */
    @NonNull
    public final ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    private void assertImportCandidate() {
        Class<?> klass = getClass();
        Class<?> interface1 = ImportSelector.class;
        Class<?> interface2 = ImportBeanDefinitionRegistrar.class;
        if (!interface1.isAssignableFrom(klass) && !interface2.isAssignableFrom(klass)) {
            String message = format("The @Import Candidate[class : '{}'] must implement the interface '{}' or '{}'",
                    klass.getName(), interface1.getName(), interface2.getName());
            throw new IllegalStateException(message);
        }
    }

    private void initializeSelf() {
        String beanName = toString();
        this.beanFactory.registerSingleton(beanName, this);
        this.beanFactory.initializeBean(this, beanName);
    }
}
