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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.EventListener;

/**
 * A listener interface for observing lifecycle events of a {@link BeanFactory}.
 * <p>
 * Implementations of this interface can be registered to receive notifications about significant
 * stages in the lifecycle of a Spring {@link BeanFactory}, such as when the bean definition registry
 * becomes available, when the bean factory is fully configured, or when the configuration is frozen.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class MyBeanFactoryListener implements BeanFactoryListener {
 *     public void onBeanDefinitionRegistryReady(BeanDefinitionRegistry registry) {
 *         System.out.println("BeanDefinitionRegistry is ready.");
 *     }
 *
 *     public void onBeanFactoryReady(ConfigurableListableBeanFactory beanFactory) {
 *         System.out.println("BeanFactory is ready.");
 *     }
 *
 *     public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory beanFactory) {
 *         System.out.println("BeanFactory configuration has been frozen.");
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EventPublishingBeanInitializer
 * @see EventPublishingBeanBeforeProcessor
 * @see EventPublishingBeanAfterProcessor
 * @see BeanFactoryListeners
 * @see BeanFactoryListenerAdapter
 * @see ConfigurableListableBeanFactory
 * @see DefaultListableBeanFactory
 * @since 1.0.0
 */
public interface BeanFactoryListener extends EventListener {

    /**
     * Handle the event when {@link BeanDefinitionRegistry} is ready
     *
     * @param registry {@link BeanDefinitionRegistry}
     */
    void onBeanDefinitionRegistryReady(BeanDefinitionRegistry registry);

    /**
     * Handle the event when {@link ConfigurableListableBeanFactory} is ready
     *
     * @param beanFactory {@link BeanDefinitionRegistry}
     */
    void onBeanFactoryReady(ConfigurableListableBeanFactory beanFactory);

    /**
     * Handle the event when the {@link ConfigurableListableBeanFactory#freezeConfiguration() BeanFactory Configuration}
     * is frozen.
     *
     * @param beanFactory {@link ConfigurableListableBeanFactory}
     * @param beanFactory {@link ConfigurableListableBeanFactory}
     */
    void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory beanFactory);

}
