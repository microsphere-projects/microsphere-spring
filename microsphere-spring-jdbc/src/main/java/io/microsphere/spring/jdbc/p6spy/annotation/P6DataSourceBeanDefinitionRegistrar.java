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
package io.microsphere.spring.jdbc.p6spy.annotation;

import com.p6spy.engine.spy.P6Factory;
import com.p6spy.engine.spy.P6ModuleManager;
import com.p6spy.engine.spy.option.P6OptionChangedListener;
import io.microsphere.spring.jdbc.p6spy.beans.factory.CompoundJdbcEventListenerFactory;
import io.microsphere.spring.jdbc.p6spy.beans.factory.config.P6DataSourceBeanPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableListableBeanFactory;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;

/**
 * The {@link ImportBeanDefinitionRegistrar} class to register {@link BeanDefinition BeanDefinitions}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class P6DataSourceBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, P6DataSourceBeanPostProcessor.class);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        initP6ModuleManager(asConfigurableListableBeanFactory(beanFactory));
    }

    private void initP6ModuleManager(ConfigurableListableBeanFactory beanFactory) {
        P6ModuleManager p6ModuleManager = P6ModuleManager.getInstance();
        addP6Factory(p6ModuleManager, beanFactory);
        registerP6OptionChangedListenerBeans(p6ModuleManager, beanFactory);
    }

    private void addP6Factory(P6ModuleManager p6ModuleManager, ConfigurableListableBeanFactory beanFactory) {
        P6Factory p6Factory = new CompoundJdbcEventListenerFactory(beanFactory);
        List<P6Factory> factories = p6ModuleManager.getFactories();
        factories.add(p6Factory);
    }

    private void registerP6OptionChangedListenerBeans(P6ModuleManager p6ModuleManager, ConfigurableListableBeanFactory beanFactory) {
        List<P6OptionChangedListener> listeners = getSortedBeans(beanFactory, P6OptionChangedListener.class);
        listeners.forEach(p6ModuleManager::registerOptionChangedListener);
    }
}
