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

import io.microsphere.spring.beans.factory.config.NamedBeanHolderComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static io.microsphere.spring.util.SpringFactoriesLoaderUtils.registerFactories;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * The Composite {@link BeanFactoryListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanFactoryListener
 * @since 1.0.0
 */
class BeanFactoryListeners implements BeanFactoryListener {

    private static final Logger logger = LoggerFactory.getLogger(BeanListeners.class);

    private static final String BEAN_NAME = "beanFactoryListeners";

    private final List<NamedBeanHolder<BeanFactoryListener>> namedListeners;

    private final int listenerCount;

    public BeanFactoryListeners(ConfigurableListableBeanFactory beanFactory) {
        this.namedListeners = getBeanDefinitionListeners(beanFactory);
        this.listenerCount = namedListeners.size();
    }

    private List<NamedBeanHolder<BeanFactoryListener>> getBeanDefinitionListeners(ConfigurableListableBeanFactory beanFactory) {
        registerFactories(BeanFactoryListener.class, beanFactory);
        Map<String, BeanFactoryListener> beanDefinitionListenersMap = beanFactory.getBeansOfType(BeanFactoryListener.class);
        List<NamedBeanHolder<BeanFactoryListener>> namedListeners = new ArrayList<>(beanDefinitionListenersMap.size());
        for (Map.Entry<String, BeanFactoryListener> entry : beanDefinitionListenersMap.entrySet()) {
            NamedBeanHolder<BeanFactoryListener> namedListener = new NamedBeanHolder<>(entry.getKey(), entry.getValue());
            namedListeners.add(namedListener);
        }
        namedListeners.sort(NamedBeanHolderComparator.INSTANCE);
        return namedListeners;
    }

    @Override
    public void onBeanDefinitionRegistryReady(BeanDefinitionRegistry registry) {
        iterate(listener -> listener.onBeanDefinitionRegistryReady(registry), "onBeanDefinitionRegistryReady");
    }

    @Override
    public void onBeanFactoryReady(ConfigurableListableBeanFactory beanFactory) {
        iterate(listener -> listener.onBeanFactoryReady(beanFactory), "onBeanFactoryReady");
    }

    @Override
    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory beanFactory) {
        iterate(listener -> listener.onBeanFactoryConfigurationFrozen(beanFactory), "onBeanFactoryConfigurationFrozen");
    }

    private void iterate(Consumer<BeanFactoryListener> listenerConsumer, String action) {
        for (int i = 0; i < listenerCount; i++) {
            NamedBeanHolder<BeanFactoryListener> namedListener = namedListeners.get(i);
            BeanFactoryListener listener = namedListener.getBeanInstance();
            String listenerBeanName = namedListener.getBeanName();
            try {
                listenerConsumer.accept(listener);
                logger.trace("BeanDefinitionListener[name : '{}' , order : {}] execution {} -> '{}'", listenerBeanName, listener, i, action);
            } catch (Throwable e) {
                logger.error("BeanDefinitionListener[name : '{}' , order : {}] execution {} -> '{}' failed", listenerBeanName, listener, i, action, e);
            }
        }
    }

    void registerBean(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder beanDefinitionBuilder = rootBeanDefinition(BeanFactoryListeners.class, () -> this);
        beanDefinitionBuilder.setPrimary(true);
        registry.registerBeanDefinition(BEAN_NAME, beanDefinitionBuilder.getBeanDefinition());
    }

    static BeanFactoryListeners getBean(BeanFactory beanFactory) {
        return beanFactory.getBean(BEAN_NAME, BeanFactoryListeners.class);
    }
}
