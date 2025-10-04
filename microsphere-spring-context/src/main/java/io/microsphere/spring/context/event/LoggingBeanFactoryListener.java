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

import io.microsphere.logging.Logger;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * A {@link BeanFactoryListener} implementation that provides logging capabilities for Spring BeanFactory events.
 * <p>
 * This class logs the key lifecycle events of a Spring BeanFactory, such as when a {@link BeanDefinitionRegistry} is ready,
 * when a {@link ConfigurableListableBeanFactory} is initialized, and when the BeanFactory configuration is frozen.
 * The logging is done using the {@link Logger} interface provided by the MicroSphere logging framework.
 * </p>
 *
 * <h3>Example Usage</h3>
 *
 * <pre>{@code
 * // Register the LoggingBeanFactoryListener with the Spring context
 * context.addBeanFactoryListener(new LoggingBeanFactoryListener());
 * }</pre>
 *
 * <p>
 * When the BeanFactory events are triggered, log messages will be generated similar to the following:
 * </p>
 *
 * <pre>
 * INFO  onBeanDefinitionRegistryReady - BeanDefinitionRegistry : org.springframework.beans.factory.support.DefaultListableBeanFactory@1f9e655
 * INFO  onBeanFactoryReady - BeanFactory : org.springframework.beans.factory.support.DefaultListableBeanFactory@1f9e655
 * INFO  onBeanFactoryConfigurationFrozen - BeanFactory : org.springframework.beans.factory.support.DefaultListableBeanFactory@1f9e655
 * </pre>
 *
 * <p>
 * These logs provide visibility into the internal state of the Spring container during startup and can be useful for debugging
 * and monitoring purposes.
 * </p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EventPublishingBeanInitializer
 * @see EventPublishingBeanBeforeProcessor
 * @see EventPublishingBeanAfterProcessor
 * @see BeanFactoryListeners
 * @see BeanFactoryListener
 * @see BeanFactoryListenerAdapter
 * @see ConfigurableListableBeanFactory
 * @see DefaultListableBeanFactory
 * @since 1.0.0
 */
public class LoggingBeanFactoryListener implements BeanFactoryListener {

    private static final Logger logger = getLogger(LoggingBeanFactoryListener.class);

    @Override
    public void onBeanDefinitionRegistryReady(BeanDefinitionRegistry registry) {
        logger.info("onBeanDefinitionRegistryReady - BeanDefinitionRegistry : {}", registry);
    }

    @Override
    public void onBeanFactoryReady(ConfigurableListableBeanFactory beanFactory) {
        logger.info("onBeanFactoryReady - BeanFactory : {}", beanFactory);
    }

    @Override
    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory beanFactory) {
        logger.info("onBeanFactoryConfigurationFrozen - BeanFactory : {}", beanFactory);
    }
}
