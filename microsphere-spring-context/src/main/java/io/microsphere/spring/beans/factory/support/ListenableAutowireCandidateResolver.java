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
package io.microsphere.spring.beans.factory.support;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.annotation.Nullable;
import io.microsphere.constants.PropertyConstants;
import io.microsphere.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.List;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asDefaultListableBeanFactory;
import static io.microsphere.spring.beans.factory.support.AutowireCandidateResolvingListener.loadListeners;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerInfrastructureBean;
import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
import static io.microsphere.util.ArrayUtils.combine;
import static java.lang.Boolean.parseBoolean;

/**
 * A decorator implementation of {@link AutowireCandidateResolver} that allows listening to the autowire candidate
 * resolution process via {@link AutowireCandidateResolvingListener}.
 *
 * <p>This class enables enhanced visibility into the Spring dependency resolution mechanism by allowing external
 * listeners to observe and react to events during autowiring. It wraps the original resolver and delegates all calls
 * to it while notifying registered listeners about various resolution stages.
 *
 * <h3>Key Features</h3>
 * <ul>
 *     <li>Wraps the existing {@link AutowireCandidateResolver} in a Spring bean factory</li>
 *     <li>Supports dynamic registration of resolving listeners</li>
 *     <li>Provides lifecycle integration through {@link BeanFactoryPostProcessor}</li>
 *     <li>Configurable via environment properties (e.g., enable/disable)</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Register as infrastructure bean
 * ListenableAutowireCandidateResolver.register(applicationContext);
 *
 * // Add custom listener
 * ListenableAutowireCandidateResolver resolver = beanFactory.getBean(ListenableAutowireCandidateResolver.class);
 * resolver.addListener((descriptor, candidates) -> {
 *     System.out.println("Resolved candidates for " + descriptor.getDependencyType());
 * });
 * }</pre>
 *
 * <h3>Configuration</h3>
 * Enable the resolver using property configuration:
 * <pre>{@code
 * microsphere.spring.listenable-autowire-candidate-resolver.enabled=true
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AutowireCandidateResolver
 * @see AutowireCandidateResolvingListener
 * @see CompositeAutowireCandidateResolvingListener
 * @see DefaultListableBeanFactory#setAutowireCandidateResolver(AutowireCandidateResolver)
 * @see BeanFactoryPostProcessor
 * @since 1.0.0
 */
public class ListenableAutowireCandidateResolver implements AutowireCandidateResolver, BeanFactoryPostProcessor,
        EnvironmentAware, BeanNameAware {

    private static final Logger logger = getLogger(ListenableAutowireCandidateResolver.class);

    /**
     * The prefix of the property name of {@link ListenableAutowireCandidateResolver}
     */
    public static final String PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "listenable-autowire-candidate-resolver.";

    private static final String DEFAULT_ENABLED = "false";

    /**
     * The property name of {@link ListenableAutowireCandidateResolver} to be 'enabled'
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_ENABLED,
            description = "The property name of ListenableAutowireCandidateResolver to be enabled or not",
            source = APPLICATION_SOURCE
    )
    public static final String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + PropertyConstants.ENABLED_PROPERTY_NAME;

    /**
     * The default property value of {@link ListenableAutowireCandidateResolver} to be 'enabled'
     */
    public static final boolean DEFAULT_ENABLED_PROPERTY_VALUE = parseBoolean(DEFAULT_ENABLED);

    private AutowireCandidateResolver delegate;

    private CompositeAutowireCandidateResolvingListener compositeListener;

    private Environment environment;

    private String beanName;

    /**
     * Adds one or more {@link AutowireCandidateResolvingListener} instances to this resolver.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ListenableAutowireCandidateResolver resolver = new ListenableAutowireCandidateResolver();
     *   resolver.addListener(new LoggingAutowireCandidateResolvingListener(),
     *       new MetricsAutowireCandidateResolvingListener());
     * }</pre>
     *
     * @param one  the first listener to add
     * @param more additional listeners to add
     */
    public void addListener(AutowireCandidateResolvingListener one, AutowireCandidateResolvingListener... more) {
        addListeners(combine(one, more));
    }

    /**
     * Adds an array of {@link AutowireCandidateResolvingListener} instances to this resolver.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ListenableAutowireCandidateResolver resolver = new ListenableAutowireCandidateResolver();
     *   AutowireCandidateResolvingListener[] listeners = {
     *       new LoggingAutowireCandidateResolvingListener()
     *   };
     *   resolver.addListeners(listeners);
     * }</pre>
     *
     * @param listeners the array of listeners to add
     */
    public void addListeners(AutowireCandidateResolvingListener[] listeners) {
        addListeners(ofList(listeners));
    }

    /**
     * Adds a list of {@link AutowireCandidateResolvingListener} instances to this resolver.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ListenableAutowireCandidateResolver resolver = new ListenableAutowireCandidateResolver();
     *   resolver.addListeners(Collections.singletonList(
     *       new LoggingAutowireCandidateResolvingListener()));
     * }</pre>
     *
     * @param listeners the list of listeners to add
     */
    public void addListeners(List<AutowireCandidateResolvingListener> listeners) {
        compositeListener.addListeners(listeners);
    }

    /**
     * Checks if a bean definition is an autowire candidate for the given dependency,
     * delegating to the underlying {@link AutowireCandidateResolver}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ListenableAutowireCandidateResolver resolver = new ListenableAutowireCandidateResolver();
     *   boolean candidate = resolver.isAutowireCandidate(beanDefinitionHolder, dependencyDescriptor);
     * }</pre>
     *
     * @param bdHolder   the bean definition holder containing the candidate bean
     * @param descriptor the descriptor for the target dependency
     * @return {@code true} if the bean is an autowire candidate, {@code false} otherwise
     */
    @Override
    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        return delegate.isAutowireCandidate(bdHolder, descriptor);
    }

    /**
     * {@inheritDoc}
     *
     * @since Spring Framework 5.0
     */
    @Override
    public boolean isRequired(DependencyDescriptor descriptor) {
        return delegate.isRequired(descriptor);
    }

    /**
     * {@inheritDoc}
     *
     * @since Spring Framework 5.1
     */
    @Override
    public boolean hasQualifier(DependencyDescriptor descriptor) {
        return delegate.hasQualifier(descriptor);
    }

    /**
     * Gets the suggested value for dependency injection, delegating to the underlying
     * {@link AutowireCandidateResolver} and notifying listeners of the resolved value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ListenableAutowireCandidateResolver resolver = new ListenableAutowireCandidateResolver();
     *   Object suggestedValue = resolver.getSuggestedValue(dependencyDescriptor);
     * }</pre>
     *
     * @param descriptor the descriptor for the target dependency
     * @return the suggested value, or {@code null} if none found
     */
    @Nullable
    @Override
    public Object getSuggestedValue(DependencyDescriptor descriptor) {
        Object suggestedValue = delegate.getSuggestedValue(descriptor);
        compositeListener.suggestedValueResolved(descriptor, suggestedValue);
        return suggestedValue;
    }

    /**
     * Gets a lazy resolution proxy for the given dependency if necessary, delegating to the
     * underlying {@link AutowireCandidateResolver} and notifying listeners of the resolved proxy.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ListenableAutowireCandidateResolver resolver = new ListenableAutowireCandidateResolver();
     *   Object proxy = resolver.getLazyResolutionProxyIfNecessary(dependencyDescriptor, "myBean");
     * }</pre>
     *
     * @param descriptor the descriptor for the target dependency
     * @param beanName   the name of the bean that is being resolved
     * @return a lazy resolution proxy, or {@code null} if not necessary
     */
    @Nullable
    @Override
    public Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, String beanName) {
        Object proxy = delegate.getLazyResolutionProxyIfNecessary(descriptor, beanName);
        compositeListener.lazyProxyResolved(descriptor, beanName, proxy);
        return proxy;
    }

    /**
     * {@inheritDoc}
     *
     * @since Spring Framework 5.2.7
     */
    @Override
    public AutowireCandidateResolver cloneIfNecessary() {
        return delegate.cloneIfNecessary();
    }

    /**
     * {@link BeanFactoryPostProcessor} callback that wraps the existing
     * {@link AutowireCandidateResolver} in the given bean factory with this listenable resolver.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ListenableAutowireCandidateResolver resolver = new ListenableAutowireCandidateResolver();
     *   resolver.postProcessBeanFactory(beanFactory);
     * }</pre>
     *
     * @param beanFactory the bean factory to post-process
     * @throws BeansException if an error occurs during post-processing
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        wrap(beanFactory);
    }

    /**
     * {@link EnvironmentAware} callback that sets the {@link Environment} on this resolver.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ListenableAutowireCandidateResolver resolver = new ListenableAutowireCandidateResolver();
     *   resolver.setEnvironment(applicationContext.getEnvironment());
     * }</pre>
     *
     * @param environment the environment to set
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * {@link BeanNameAware} callback that sets the bean name of this resolver in the bean factory.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ListenableAutowireCandidateResolver resolver = new ListenableAutowireCandidateResolver();
     *   resolver.setBeanName("listenableAutowireCandidateResolver");
     * }</pre>
     *
     * @param name the name of this bean in the bean factory
     */
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    /**
     * Wraps {@link AutowireCandidateResolver} as the {@link ListenableAutowireCandidateResolver} and then register to
     * the given {@link DefaultListableBeanFactory}
     *
     * @param beanFactory {@link DefaultListableBeanFactory}
     */
    public void wrap(BeanFactory beanFactory) {
        if (!isEnabled(this.environment)) {
            if (logger.isInfoEnabled()) {
                logger.info("The ListenableAutowireCandidateResolver bean[name : '{}'] is disabled.", this.beanName);
                logger.info("Setting the configuration property '{} = true' to enable it if requires.", ENABLED_PROPERTY_NAME);
            }
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("The ListenableAutowireCandidateResolver bean[name : '{}'] is enabled.", this.beanName);
        }
        DefaultListableBeanFactory dbf = asDefaultListableBeanFactory(beanFactory);
        AutowireCandidateResolver autowireCandidateResolver = dbf.getAutowireCandidateResolver();
        if (autowireCandidateResolver != this) {
            List<AutowireCandidateResolvingListener> listeners = loadListeners(beanFactory);
            CompositeAutowireCandidateResolvingListener compositeListener = new CompositeAutowireCandidateResolvingListener(listeners);
            this.delegate = autowireCandidateResolver;
            this.compositeListener = compositeListener;
            dbf.setAutowireCandidateResolver(this);
        }
    }

    /**
     * Determine whether the {@link ListenableAutowireCandidateResolver} is enabled or not
     *
     * @param environment {@link Environment}
     * @return <code>true</code> if enabled, otherwise <code>false</code>
     */
    public static boolean isEnabled(Environment environment) {
        return environment.getProperty(ENABLED_PROPERTY_NAME, boolean.class, DEFAULT_ENABLED_PROPERTY_VALUE);
    }

    /**
     * Register the {@link ListenableAutowireCandidateResolver} as the infrastructure bean
     *
     * @param applicationContext {@link ConfigurableApplicationContext}
     */
    public static void register(ConfigurableApplicationContext applicationContext) {
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        BeanDefinitionRegistry beanDefinitionRegistry = asBeanDefinitionRegistry(beanFactory);
        registerInfrastructureBean(beanDefinitionRegistry, ListenableAutowireCandidateResolver.class);
    }

}
