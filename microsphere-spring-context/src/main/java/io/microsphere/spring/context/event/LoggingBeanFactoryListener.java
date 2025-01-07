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

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * Logging {@link BeanFactoryListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanFactoryListener
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
