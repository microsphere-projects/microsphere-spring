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

import io.microsphere.spring.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.ResolvableType;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import static io.microsphere.spring.context.event.BeanListeners.getBean;
import static io.microsphere.spring.context.event.BeanListeners.getReadyBeanNames;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static org.springframework.util.ReflectionUtils.doWithFields;

/**
 * Bean After-Event Publishing Processor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EventPublishingBeanBeforeProcessor
 * @see BeanListeners
 * @see BeanListener
 * @see BeanListenerAdapter
 * @see BeanFactoryListeners
 * @see BeanFactoryListener
 * @see BeanFactoryListenerAdapter
 * @see EventPublishingBeanInitializer
 * @since 1.0.0
 */
class EventPublishingBeanAfterProcessor extends InstantiationAwareBeanPostProcessorAdapter implements GenericApplicationListenerAdapter {

    private static final Class<?> DISPOSABLE_BEAN_ADAPTER_CLASS = resolveClass("org.springframework.beans.factory.support.DisposableBeanAdapter");

    private final ConfigurableApplicationContext context;

    private final BeanListeners beanEventListeners;

    public EventPublishingBeanAfterProcessor(ConfigurableApplicationContext context) {
        this.context = context;
        this.beanEventListeners = getBean(context);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        this.beanEventListeners.onAfterBeanInitialized(beanName, bean);
        return bean;
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ContextRefreshedEvent.class.isAssignableFrom(eventType) || ContextClosedEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        return eventType.isAssignableFrom(ContextRefreshedEvent.class) || eventType.isAssignableFrom(ContextClosedEvent.class);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (!Objects.equals(context, event.getSource())) {
            return;
        }
        if (event instanceof ContextRefreshedEvent) {
            onContextRefreshedEvent((ContextRefreshedEvent) event);
        } else if (event instanceof ContextClosedEvent) {
            onContextClosedEvent((ContextClosedEvent) event);
        }
    }

    private void onContextRefreshedEvent(ContextRefreshedEvent event) {
        fireBeansReadyEvent();
    }

    private void fireBeansReadyEvent() {
        ConfigurableListableBeanFactory beanFactory = this.context.getBeanFactory();
        Map<String, Object> beansMap = beanFactory.getBeansOfType(Object.class, true, false);
        for (Entry<String, Object> beanEntry : beansMap.entrySet()) {
            String beanName = beanEntry.getKey();
            Object bean = beanEntry.getValue();
            fireBeanReadyEvent(beanName, bean);
        }
    }

    private void fireBeanReadyEvent(String beanName, Object bean) {
        this.beanEventListeners.onBeanReady(beanName, bean);
    }

    private void onContextClosedEvent(ContextClosedEvent event) {
        decorateDisposableBeans();
    }

    private void decorateDisposableBeans() {
        ConfigurableListableBeanFactory beanFactory = this.context.getBeanFactory();
        if (beanFactory instanceof DefaultSingletonBeanRegistry) {
            doWithFields(DefaultSingletonBeanRegistry.class, field -> {
                field.setAccessible(true);
                Map<String, Object> disposableBeans = (Map<String, Object>) field.get(beanFactory);
                for (Entry<String, Object> entry : disposableBeans.entrySet()) {
                    String beanName = entry.getKey();
                    Object adapterBean = entry.getValue();
                    if (isDisposableBeanAdapter(adapterBean)) {
                        DisposableBean delegate = (DisposableBean) adapterBean;
                        DecoratingDisposableBean decoratingDisposableBean = new DecoratingDisposableBean(beanName, delegate, this.beanEventListeners::onAfterBeanDestroy);
                        entry.setValue(decoratingDisposableBean);
                    }
                }
            }, field -> "disposableBeans".equals(field.getName()) && Map.class.isAssignableFrom(field.getType()));
        }
    }

    private boolean isDisposableBeanAdapter(Object bean) {
        return DISPOSABLE_BEAN_ADAPTER_CLASS.equals(bean.getClass()) && bean instanceof DisposableBean;
    }


    /**
     * {@link EventPublishingBeanBeforeProcessor} Initializer that
     * is not a general propose Spring Bean initializes {@link EventPublishingBeanBeforeProcessor}
     */
    static class Initializer {

        public Initializer(ConfigurableApplicationContext context) {
            EventPublishingBeanAfterProcessor processor = new EventPublishingBeanAfterProcessor(context);
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            beanFactory.addBeanPostProcessor(processor);
            context.addApplicationListener(processor);
            fireBeanFactoryConfigurationFrozenEvent(beanFactory);
            fireBeanDefinitionReadyEvent(beanFactory);
        }

        private void fireBeanFactoryConfigurationFrozenEvent(ConfigurableListableBeanFactory beanFactory) {
            BeanFactoryListeners beanFactoryListeners = BeanFactoryListeners.getBean(beanFactory);
            beanFactoryListeners.onBeanFactoryConfigurationFrozen(beanFactory);
        }

        private void fireBeanDefinitionReadyEvent(ConfigurableListableBeanFactory beanFactory) {

            String[] beanNames = beanFactory.getBeanDefinitionNames();

            BeanListeners beanEventListeners = getBean(beanFactory);
            Set<String> readyBeanNames = getReadyBeanNames(beanFactory);
            beanEventListeners.setReadyBeanNames(readyBeanNames);
            for (String beanName : beanNames) {
                BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
                if (beanDefinition instanceof RootBeanDefinition) {
                    beanEventListeners.onBeanDefinitionReady(beanName, (RootBeanDefinition) beanDefinition);
                }
            }
        }
    }

    private static class DecoratingDisposableBean implements DisposableBean {

        private final String beanName;

        private final DisposableBean delegate;

        private final BiConsumer<String, Object> destroyedCallback;

        DecoratingDisposableBean(String beanName, DisposableBean delegate, BiConsumer<String, Object> destroyedCallback) {
            this.beanName = beanName;
            this.delegate = delegate;
            this.destroyedCallback = destroyedCallback;
        }

        @Override
        public void destroy() throws Exception {
            this.delegate.destroy();
            doWithFields(DISPOSABLE_BEAN_ADAPTER_CLASS, field -> {
                field.setAccessible(true);
                Object bean = field.get(this.delegate);
                this.destroyedCallback.accept(this.beanName, bean);
            }, field -> "bean".equals(field.getName()) && Object.class.equals(field.getType()));
        }
    }
}
