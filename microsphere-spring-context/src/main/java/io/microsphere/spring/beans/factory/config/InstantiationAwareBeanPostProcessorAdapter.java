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
package io.microsphere.spring.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.lang.Nullable;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;

/**
 * The Adapter class of {@link SmartInstantiationAwareBeanPostProcessor} is compatible with
 * Spring [3.x,)
 * {@linkplain org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter} was deprecated
 * since Spring 5.3, and removed since Spring 6.x.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SmartInstantiationAwareBeanPostProcessor
 * @see InstantiationAwareBeanPostProcessor
 * @since 1.0.0
 */
public abstract class InstantiationAwareBeanPostProcessorAdapter implements SmartInstantiationAwareBeanPostProcessor {

    @Nullable
    public Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    /**
     * Determine the type of the bean to be eventually returned from this
     * processor's {@link #postProcessBeforeInstantiation} callback.
     * <p>The default implementation returns the given bean class as-is.
     * Specific implementations should fully evaluate their processing steps
     * in order to create/initialize a potential proxy class upfront.
     *
     * @param beanClass the raw class of the bean
     * @param beanName  the name of the bean
     * @return the type of the bean (never {@code null})
     * @throws org.springframework.beans.BeansException in case of errors
     * @since 6.0
     */
    public Class<?> determineBeanType(Class<?> beanClass, String beanName) throws BeansException {
        return beanClass;
    }

    /**
     * Determine the candidate constructors to use for the given bean.
     * <p>The default implementation returns {@code null}.
     *
     * @param beanClass the raw class of the bean (never {@code null})
     * @param beanName  the name of the bean
     * @return the candidate constructors, or {@code null} if none specified
     * @throws org.springframework.beans.BeansException in case of errors
     */
    @Nullable
    public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName)
            throws BeansException {
        return null;
    }

    /**
     * Obtain a reference for early access to the specified bean,
     * typically for the purpose of resolving a circular reference.
     * <p>This callback gives post-processors a chance to expose a wrapper
     * early - that is, before the target bean instance is fully initialized.
     * The exposed object should be equivalent to the what
     * {@link #postProcessBeforeInitialization} / {@link #postProcessAfterInitialization}
     * would expose otherwise. Note that the object returned by this method will
     * be used as bean reference unless the post-processor returns a different
     * wrapper from said post-process callbacks. In other words: Those post-process
     * callbacks may either eventually expose the same reference or alternatively
     * return the raw bean instance from those subsequent callbacks (if the wrapper
     * for the affected bean has been built for a call to this method already,
     * it will be exposes as final bean reference by default).
     * <p>The default implementation returns the given {@code bean} as-is.
     *
     * @param bean     the raw bean instance
     * @param beanName the name of the bean
     * @return the object to expose as bean reference
     * (typically with the passed-in bean instance as default)
     * @throws org.springframework.beans.BeansException in case of errors
     */
    public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * Apply this BeanPostProcessor <i>before the target bean gets instantiated</i>.
     * The returned bean object may be a proxy to use instead of the target bean,
     * effectively suppressing default instantiation of the target bean.
     * <p>If a non-null object is returned by this method, the bean creation process
     * will be short-circuited. The only further processing applied is the
     * {@link #postProcessAfterInitialization} callback from the configured
     * {@link BeanPostProcessor BeanPostProcessors}.
     * <p>This callback will be applied to bean definitions with their bean class,
     * as well as to factory-method definitions in which case the returned bean type
     * will be passed in here.
     * <p>Post-processors may implement the extended
     * {@link SmartInstantiationAwareBeanPostProcessor} interface in order
     * to predict the type of the bean object that they are going to return here.
     * <p>The default implementation returns {@code null}.
     *
     * @param beanClass the class of the bean to be instantiated
     * @param beanName  the name of the bean
     * @return the bean object to expose instead of a default instance of the target bean,
     * or {@code null} to proceed with default instantiation
     * @throws org.springframework.beans.BeansException in case of errors
     * @see #postProcessAfterInstantiation
     * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getBeanClass()
     * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getFactoryMethodName()
     */
    @Nullable
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    /**
     * Perform operations after the bean has been instantiated, via a constructor or factory method,
     * but before Spring property population (from explicit properties or autowiring) occurs.
     * <p>This is the ideal callback for performing custom field injection on the given bean
     * instance, right before Spring's autowiring kicks in.
     * <p>The default implementation returns {@code true}.
     *
     * @param bean     the bean instance created, with properties not having been set yet
     * @param beanName the name of the bean
     * @return {@code true} if properties should be set on the bean; {@code false}
     * if property population should be skipped. Normal implementations should return {@code true}.
     * Returning {@code false} will also prevent any subsequent InstantiationAwareBeanPostProcessor
     * instances being invoked on this bean instance.
     * @throws org.springframework.beans.BeansException in case of errors
     * @see #postProcessBeforeInstantiation
     */
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        return true;
    }

    /**
     * Post-process the given property values before the factory applies them
     * to the given bean. Allows for checking whether all dependencies have been
     * satisfied, for example based on a "Required" annotation on bean property setters.
     * <p>Also allows for replacing the property values to apply, typically through
     * creating a new MutablePropertyValues instance based on the original PropertyValues,
     * adding or removing specific values.
     * <p>The default implementation returns the given {@code pvs} as-is.
     * @param pvs the property values that the factory is about to apply (never {@code null})
     * @param pds the relevant property descriptors for the target bean (with ignored
     * dependency types - which the factory handles specifically - already filtered out)
     * @param bean the bean instance created, but whose properties have not yet been set
     * @param beanName the name of the bean
     * @return the actual property values to apply to the given bean (can be the passed-in
     * PropertyValues instance), or {@code null} to skip property population
     * @throws org.springframework.beans.BeansException in case of errors
     * @see #postProcessProperties
     * @see org.springframework.beans.MutablePropertyValues
     * @deprecated as of 5.1, in favor of {@link #postProcessProperties(PropertyValues, Object, String)}
     */
    public PropertyValues postProcessPropertyValues(
            PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeanCreationException {
        return pvs;
    }

    /**
     * Post-process the given property values before the factory applies them
     * to the given bean.
     * <p>The default implementation returns the given {@code pvs} as-is.
     *
     * @param pvs      the property values that the factory is about to apply (never {@code null})
     * @param bean     the bean instance created, but whose properties have not yet been set
     * @param beanName the name of the bean
     * @return the actual property values to apply to the given bean (can be the passed-in
     * PropertyValues instance), or {@code null} to skip property population
     * @throws org.springframework.beans.BeansException in case of errors
     * @since 5.1
     */
    @Nullable
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
            throws BeansException {
        return pvs;
    }

}
