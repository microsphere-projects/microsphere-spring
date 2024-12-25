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

import io.microsphere.constants.PropertyConstants;
import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;
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

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;

import static io.microsphere.invoke.MethodHandleUtils.findVirtual;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asDefaultListableBeanFactory;
import static io.microsphere.spring.beans.factory.support.AutowireCandidateResolvingListener.loadListeners;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerInfrastructureBean;
import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
import static io.microsphere.util.ArrayUtils.combine;
import static org.springframework.beans.BeanUtils.instantiateClass;

/**
 * The decorator class of {@link AutowireCandidateResolver} to listen to the resolving process of autowire candidate by
 * {@link AutowireCandidateResolvingListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AutowireCandidateResolver
 * @see AutowireCandidateResolvingListener
 * @see CompositeAutowireCandidateResolvingListener
 * @see DefaultListableBeanFactory#setAutowireCandidateResolver(AutowireCandidateResolver)
 * @see BeanFactoryPostProcessor
 * @since 1.0.0
 */
public class ListenableAutowireCandidateResolver implements AutowireCandidateResolver, BeanFactoryPostProcessor,
        EnvironmentAware, BeanNameAware {

    private static final Logger logger = LoggerFactory.getLogger(ListenableAutowireCandidateResolver.class);

    /**
     * The name of the method name of {@link AutowireCandidateResolver#cloneIfNecessary()}
     *
     * @since Spring Framework 5.2.7
     */
    private static final String CLONE_IF_NECESSARY_METHOD_NAME = "cloneIfNecessary";

    /**
     * The {@link MethodHandle} of {@link AutowireCandidateResolver#cloneIfNecessary()}
     *
     * @since Spring Framework 5.2.7
     */
    private static final MethodHandle CLONE_IF_NECESSARY_METHOD_HANDLE = findVirtual(AutowireCandidateResolver.class, CLONE_IF_NECESSARY_METHOD_NAME);

    /**
     * The prefix of the property name of {@link ListenableAutowireCandidateResolver}
     */
    public static final String PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "listenable-autowire-candidate-resolver.";

    /**
     * The property name of {@link ListenableAutowireCandidateResolver} to be 'enabled'
     */
    public static final String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + PropertyConstants.ENABLED_PROPERTY_NAME;

    /**
     * The default property value of {@link ListenableAutowireCandidateResolver} to be 'enabled'
     */
    public static final boolean ENABLED_PROPERTY_VALUE = false;

    private AutowireCandidateResolver delegate;

    private CompositeAutowireCandidateResolvingListener compositeListener;

    private Environment environment;

    private String beanName;

    public void addListener(AutowireCandidateResolvingListener one, AutowireCandidateResolvingListener... more) {
        addListeners(combine(one, more));
    }

    public void addListeners(AutowireCandidateResolvingListener[] listeners) {
        addListeners(Arrays.asList(listeners));
    }

    public void addListeners(List<AutowireCandidateResolvingListener> listeners) {
        compositeListener.addListeners(listeners);
    }

    @Override
    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        return delegate.isAutowireCandidate(bdHolder, descriptor);
    }

    @Override
    public boolean isRequired(DependencyDescriptor descriptor) {
        return delegate.isRequired(descriptor);
    }

    /**
     * {@inheritDoc}
     *
     * @since Spring Framework 5.1
     */
    public boolean hasQualifier(DependencyDescriptor descriptor) {
        return invokeMethod(descriptor, "hasQualifier", descriptor);
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
        if (CLONE_IF_NECESSARY_METHOD_HANDLE != null) {
            return execute(() -> (AutowireCandidateResolver) CLONE_IF_NECESSARY_METHOD_HANDLE.invokeExact(delegate));
        }
        if (logger.isTraceEnabled()) {
            logger.trace("The method AutowireCandidateResolver#cloneIfNecessary() was not found, the clone instance will be created on default way.");
        }
        return instantiateClass(delegate.getClass());
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
        if (logger.isDebugEnabled()) {
            logger.debug("The ListenableAutowireCandidateResolver bean[name : '{}'] is enabled.", this.beanName);
        }
        DefaultListableBeanFactory dbf = asDefaultListableBeanFactory(beanFactory);
        AutowireCandidateResolver autowireCandidateResolver = dbf.getAutowireCandidateResolver();
        if (autowireCandidateResolver != null) {
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
        return environment.getProperty(ENABLED_PROPERTY_NAME, boolean.class, ENABLED_PROPERTY_VALUE);
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
