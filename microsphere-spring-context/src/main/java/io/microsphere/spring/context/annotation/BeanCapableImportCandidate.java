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

import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableListableBeanFactory;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBean;
import static io.microsphere.spring.core.env.EnvironmentUtils.asConfigurableEnvironment;
import static io.microsphere.text.FormatUtils.format;
import static java.lang.Integer.toHexString;

/**
 * The {@link Import @Import} candidate is an instance of {@link org.springframework.context.annotation.ImportSelector} or {@link ImportBeanDefinitionRegistrar}
 * and not a regular Spring bean, which only invokes {@link BeanClassLoaderAware}, {@link BeanFactoryAware},
 * {@link EnvironmentAware}, and {@link ResourceLoaderAware} contracts in order if they are implemented, thus it will
 * not be {@link AbstractAutowireCapableBeanFactory#populateBean(String, RootBeanDefinition, BeanWrapper) populated} and
 * {@link AutowireCapableBeanFactory#initializeBean(Object, String) initialized} .
 * <p>
 * The current abstract implementation is a template class supports the Spring bean lifecycles :
 * {@link AbstractAutowireCapableBeanFactory#populateBean(String, RootBeanDefinition, BeanWrapper) population},
 * {@link AutowireCapableBeanFactory#initializeBean(Object, String) initialization}, and
 * {@link ConfigurableBeanFactory#destroyBean(String, Object) destroy}, the sub-class must implement
 * the interface {@link ImportSelector} or {@link ImportBeanDefinitionRegistrar}, and can't override those methods:
 * <ul>
 *     <li>{@link #setBeanClassLoader(ClassLoader)}</li>
 *     <li>{@link #setBeanFactory(BeanFactory)}</li>
 *     <li>{@link #setEnvironment(Environment)}</li>
 *     <li>{@link #setResourceLoader(ResourceLoader)}</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 *
 * <pre>{@code
 * public class MyImportRegistrar extends BeanCapableImportCandidate implements ImportBeanDefinitionRegistrar {
 *
 *     private final Logger logger = getLogger(this.getClass());
 *
 *     public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
 *         logger.info("Registering beans from custom registrar");
 *         registry.registerBeanDefinition("myBean", new RootBeanDefinition(MyBean.class));
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractAutowireCapableBeanFactory#populateBean(String, RootBeanDefinition, BeanWrapper)
 * @see AutowireCapableBeanFactory#initializeBean(Object, String)
 * @see ConfigurableBeanFactory#destroyBean(String, Object)
 * @see org.springframework.context.support.ApplicationContextAwareProcessor
 * @since 1.0.0
 */
public abstract class BeanCapableImportCandidate implements BeanClassLoaderAware, BeanFactoryAware, EnvironmentAware,
        ResourceLoaderAware {

    protected final Logger logger = getLogger(this.getClass());

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
            this.beanFactory = asConfigurableListableBeanFactory(beanFactory);
        }
    }

    @Override
    public final void setEnvironment(Environment environment) {
        if (this.environment == null) {
            this.environment = asConfigurableEnvironment(environment);
        }
    }

    @Override
    public final void setResourceLoader(ResourceLoader resourceLoader) {
        if (this.resourceLoader == null) {
            this.resourceLoader = resourceLoader;
            initializeSelfAsBean();
        }
    }

    /**
     * Get the {@link ClassLoader} instance
     *
     * @return non-null
     */
    @Nonnull
    public final ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * The {@link ConfigurableListableBeanFactory} instance
     *
     * @return non-null
     */
    @Nonnull
    public final ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    /**
     * The {@link ConfigurableEnvironment} instance
     *
     * @return non-null
     */
    @Nonnull
    public final ConfigurableEnvironment getEnvironment() {
        return environment;
    }

    /**
     * The {@link ResourceLoader} instance
     *
     * @return non-null
     */
    @Nonnull
    public final ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    private void assertImportCandidate() {
        Class<?> klass = getClass();
        Class<?> interface1 = ImportSelector.class;
        Class<?> interface2 = ImportBeanDefinitionRegistrar.class;
        if (!interface1.isAssignableFrom(klass) && !interface2.isAssignableFrom(klass)) {
            String message = format("The @Import Candidate[class : '{}'] must implement the interface '{}' or '{}'", klass.getName(), interface1.getName(), interface2.getName());
            throw new IllegalStateException(message);
        }
    }

    private void initializeSelfAsBean() {
        String beanName = getClass().getName() + "@" + toHexString(hashCode());
        BeanDefinitionRegistry registry = asBeanDefinitionRegistry(this.beanFactory);
        // register the current instance as a Spring BeanDefinition
        registerBean(registry, beanName, this);
        // initialize bean from the BeanDefinition before registration
        this.beanFactory.getBean(beanName);
    }
}
