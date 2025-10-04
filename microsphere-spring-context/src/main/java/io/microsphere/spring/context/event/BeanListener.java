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
package io.microsphere.spring.context.event;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.EventListener;

/**
 * Listener interface for bean lifecycle events.
 *
 * <p>Implementations of this interface can be used to receive notifications at various stages
 * of a bean's lifecycle, such as before or after instantiation, initialization, or destruction.
 * This interface provides a single extension point to react to different bean lifecycle phases.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class MyBeanListener implements BeanListener {
 *
 *     @Override
 *     public boolean supports(String beanName) {
 *         return "myBean".equals(beanName); // listen only to "myBean"
 *     }
 *
 *     @Override
 *     public void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
 *         System.out.println("Bean definition is ready: " + beanName);
 *     }
 *
 *     @Override
 *     public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
 *         System.out.println("Before instantiating bean: " + beanName);
 *     }
 *
 *     // Implement other lifecycle methods as needed...
 * }
 * }</pre>
 *
 * <p><b>Note:</b> Implementations should check if they support a particular bean by implementing
 * the {@link #supports(String)} method. Returning {@code false} will skip further lifecycle events
 * for that bean.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EventPublishingBeanInitializer
 * @see EventPublishingBeanBeforeProcessor
 * @see EventPublishingBeanAfterProcessor
 * @see BeanListeners
 * @see BeanListenerAdapter
 * @since 1.0.0
 */
public interface BeanListener extends EventListener {

    /**
     * Supports the bean to be listened or not
     *
     * @param beanName the name of bean
     * @return If <code>false</code>, any method of bean lifecycle will not be called
     */
    boolean supports(String beanName);

    /**
     * Handle the event when the {@link RootBeanDefinition BeanDefinition} is ready(merged)
     *
     * @param beanName             the bean name
     * @param mergedBeanDefinition the merged {@link RootBeanDefinition BeanDefinition}
     */
    void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition);

    /**
     * Handle the event before the bean instantiation
     *
     * @param beanName             the bean name
     * @param mergedBeanDefinition the merged {@link RootBeanDefinition BeanDefinition}
     */
    void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition);

    /**
     * Handle the event before the bean instantiation using the {@link Constructor constructor}
     *
     * @param beanName             the bean name
     * @param mergedBeanDefinition the merged {@link RootBeanDefinition BeanDefinition}
     * @param constructor          the {@link Constructor constructor} for bean instantiation
     * @param args                 the arguments of {@link Constructor constructor}
     */
    void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Constructor<?> constructor, Object[] args);

    /**
     * Handle the event before the bean instantiation using the factory method
     *
     * @param beanName             the bean name
     * @param mergedBeanDefinition the merged {@link RootBeanDefinition BeanDefinition}
     * @param factoryBean          the factory bean(optional)
     * @param factoryMethod        the factory {@link Method} for bean instantiation
     */
    void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Object factoryBean, Method factoryMethod, Object[] args);

    /**
     * Handle the event before after the bean instantiation
     *
     * @param beanName             the bean name
     * @param mergedBeanDefinition the merged {@link RootBeanDefinition BeanDefinition}
     * @param bean                 the bean has been instantiated, not initialized yet
     */
    void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean);

    /**
     * Handle the event when the Beans' {@link PropertyValues} is ready
     *
     * @param beanName the bean name
     * @param bean     the bean has been instantiated, not initialized yet
     * @param pvs      the {@link PropertyValues} will be applied on bean
     */
    void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs);

    /**
     * Handle the event before the bean initialization
     *
     * @param beanName the bean name
     * @param bean     the bean is not initialized yet
     */
    void onBeforeBeanInitialize(String beanName, Object bean);

    /**
     * Handle the event after the bean initialization
     *
     * @param beanName the bean name
     * @param bean     the bean has been initialized
     */
    void onAfterBeanInitialized(String beanName, Object bean);

    /**
     * Handle the event on the bean ready
     *
     * @param beanName the bean name
     * @param bean     the bean is prepared
     */
    void onBeanReady(String beanName, Object bean);

    /**
     * Handle the event before the bean destroy
     *
     * @param beanName the bean name
     * @param bean     the bean is to be destroyed
     */
    void onBeforeBeanDestroy(String beanName, Object bean);

    /**
     * Handle the event after the bean destroy
     *
     * @param beanName the bean name
     * @param bean     the bean is destroyed
     */
    void onAfterBeanDestroy(String beanName, Object bean);
}
