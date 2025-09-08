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
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;

/**
 * The Adapter class of {@link SmartInstantiationAwareBeanPostProcessor} is compatible with Spring [3.x,)
 * {@linkplain org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter} was deprecated
 * since Spring 5.3, and removed since Spring 6.x.
 * This class serves as a base for implementing custom bean post-processing logic
 * during instantiation.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class MyBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {
 *     @Override
 *     public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
 *         // Custom logic to execute before bean instantiation.
 *         return super.postProcessBeforeInstantiation(beanClass, beanName);
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SmartInstantiationAwareBeanPostProcessor
 * @see InstantiationAwareBeanPostProcessor
 * @since 1.0.0
 */
public abstract class InstantiationAwareBeanPostProcessorAdapter implements SmartInstantiationAwareBeanPostProcessor {

    /**
     * {@inheritDoc}
     */
    public Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @since Spring Framework 6.0
     */
    @Override
    public Class<?> determineBeanType(Class<?> beanClass, String beanName) throws BeansException {
        return beanClass;
    }

    /**
     * {@inheritDoc}
     */
    public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName)
            throws BeansException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * {@inheritDoc}
     */
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated as of Spring Framework 5.1, in favor of {@link #postProcessProperties(PropertyValues, Object, String)}
     */
    public PropertyValues postProcessPropertyValues(
            PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeanCreationException {
        return pvs;
    }

    /**
     * {@inheritDoc}
     *
     * @since Spring Framework 5.1
     */
    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
            throws BeansException {
        return pvs;
    }

    /**
     * {@inheritDoc}
     */
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * {@inheritDoc}
     */
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
