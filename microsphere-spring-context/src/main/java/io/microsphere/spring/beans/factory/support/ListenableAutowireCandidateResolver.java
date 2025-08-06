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

import java.lang.invoke.MethodHandle;
import java.util.List;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.invoke.MethodHandleUtils.findVirtual;
import static io.microsphere.invoke.MethodHandleUtils.handleInvokeExactFailure;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asDefaultListableBeanFactory;
import static io.microsphere.spring.beans.factory.support.AutowireCandidateResolvingListener.loadListeners;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerInfrastructureBean;
import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
import static io.microsphere.util.ArrayUtils.combine;
import static java.lang.Boolean.parseBoolean;
import static org.springframework.beans.BeanUtils.instantiateClass;

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
 * @since 1.0.0
 */
public class ListenableAutowireCandidateResolver implements AutowireCandidateResolver, BeanFactoryPostProcessor,
        EnvironmentAware, BeanNameAware {

    private static final Logger logger = getLogger(ListenableAutowireCandidateResolver.class);

    /**
     * The {@link MethodHandle} of {@link AutowireCandidateResolver#isRequired(DependencyDescriptor)}
     *
     * @since Spring Framework 5.0
     */
    private static final MethodHandle IS_REQUIRED_METHOD_HANDLE = findVirtual(AutowireCandidateResolver.class, "isRequired", DependencyDescriptor.class);

    /**
     * The {@link MethodHandle} of {@link AutowireCandidateResolver#hasQualifier(DependencyDescriptor)}
     *
     * @since Spring Framework 5.1
     */
    private static final MethodHandle HAS_QUALIFIER_METHOD_HANDLE = findVirtual(AutowireCandidateResolver.class, "hasQualifier", DependencyDescriptor.class);

    /**
     * The {@link MethodHandle} of {@link AutowireCandidateResolver#cloneIfNecessary()}
     *
     * @since Spring Framework 5.2.7
     */
    private static final MethodHandle CLONE_IF_NECESSARY_METHOD_HANDLE = findVirtual(AutowireCandidateResolver.class, "cloneIfNecessary");

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
            description = "The property name of ListenableAutowireCandidateResolver to be enabled or not"
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

    public void addListener(AutowireCandidateResolvingListener one, AutowireCandidateResolvingListener... more) {
        addListeners(combine(one, more));
    }

    public void addListeners(AutowireCandidateResolvingListener[] listeners) {
        addListeners(ofList(listeners));
    }

    public void addListeners(List<AutowireCandidateResolvingListener> listeners) {
        compositeListener.addListeners(listeners);
    }

    @Override
    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        return delegate.isAutowireCandidate(bdHolder, descriptor);
    }

    /**
     * {@inheritDoc}
     *
     * @since Spring Framework 5.0
     */
    public boolean isRequired(DependencyDescriptor descriptor) {
        MethodHandle methodHandle = IS_REQUIRED_METHOD_HANDLE;
        if (methodHandle == null) {
            return descriptor.isRequired();
        }
        boolean required = false;
        try {
            required = (boolean) methodHandle.invokeExact(delegate, descriptor);
        } catch (Throwable e) {
            handleInvokeExactFailure(e, methodHandle, descriptor, descriptor);
            required = descriptor.isRequired();
        }
        return required;
    }

    /**
     * {@inheritDoc}
     *
     * @since Spring Framework 5.1
     */
    public boolean hasQualifier(DependencyDescriptor descriptor) {
        MethodHandle methodHandle = HAS_QUALIFIER_METHOD_HANDLE;
        if (methodHandle == null) {
            return false;
        }
        boolean hasQualifier = false;
        try {
            hasQualifier = (boolean) methodHandle.invokeExact(delegate, descriptor);
        } catch (Throwable e) {
            handleInvokeExactFailure(e, methodHandle, delegate, descriptor);
        }
        return hasQualifier;
    }

    @Nullable
    @Override
    public Object getSuggestedValue(DependencyDescriptor descriptor) {
        Object suggestedValue = delegate.getSuggestedValue(descriptor);
        compositeListener.suggestedValueResolved(descriptor, suggestedValue);
        return suggestedValue;
    }

    @Nullable
    @Override
    public Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, String beanName) {
        Object proxy = delegate.getLazyResolutionProxyIfNecessary(descriptor, beanName);
        compositeListener.lazyProxyResolved(descriptor, beanName, proxy);
        return proxy;
    }

    /**
     * Clone the delegate {@link AutowireCandidateResolver} if necessary
     * No {@link Override} was marked in order to be compatible with the Spring 4.x
     *
     * @return {@link AutowireCandidateResolver}
     * @since Spring Framework 5.2.7
     */
    public AutowireCandidateResolver cloneIfNecessary() {
        MethodHandle methodHandle = CLONE_IF_NECESSARY_METHOD_HANDLE;
        if (methodHandle == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("The method AutowireCandidateResolver#cloneIfNecessary() was not found, the clone instance will be created on default way.");
            }
            return instantiateClass(delegate.getClass());
        }
        AutowireCandidateResolver autowireCandidateResolver = null;
        try {
            autowireCandidateResolver = (AutowireCandidateResolver) methodHandle.invokeExact(delegate);
        } catch (Throwable e) {
            handleInvokeExactFailure(e, methodHandle, delegate);
        }
        return autowireCandidateResolver;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        wrap(beanFactory);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

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
