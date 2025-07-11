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
package io.microsphere.spring.context.annotation;

import io.microsphere.logging.Logger;
import io.microsphere.spring.context.event.InterceptingApplicationEventMulticaster;
import io.microsphere.spring.context.event.InterceptingApplicationEventMulticasterProxy;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Objects;
import java.util.concurrent.Executor;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.context.annotation.EnableEventExtension.NO_EXECUTOR;
import static io.microsphere.spring.context.annotation.EventExtensionAttributes.EXECUTOR_FOR_LISTENER_ATTRIBUTE_NAME;
import static io.microsphere.spring.context.annotation.EventExtensionAttributes.INTERCEPTED_ATTRIBUTE_NAME;
import static io.microsphere.spring.context.event.InterceptingApplicationEventMulticasterProxy.getResetBeanName;
import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

/**
 * Event Management Registrar
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableEventExtension
 * @see InterceptingApplicationEventMulticaster
 * @see SimpleApplicationEventMulticaster
 * @see Executor
 * @see TaskExecutor
 * @since 1.0.0
 */
class EventExtensionRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private static final Logger logger = getLogger(EventExtensionRegistrar.class);

    /**
     * The attribute name of the name of {@link EnableEventExtension} annotated on the class
     */
    static final String CLASS_NAME_ATTRIBUTE_NAME = "@className";

    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        registerApplicationEventMulticaster(metadata, registry);
    }

    void registerApplicationEventMulticaster(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        EventExtensionAttributes attributes = new EventExtensionAttributes(metadata, this.environment);

        boolean intercepted = attributes.isIntercepted();
        String executorForListener = attributes.getExecutorForListener();

        boolean associatedExecutorBean = !NO_EXECUTOR.equals(executorForListener);

        String beanName = APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

        if (!intercepted && !associatedExecutorBean) {
            if (logger.isInfoEnabled()) {
                logger.info("The ApplicationEventMulticaster bean[name : '{}'] will not be registered, caused by {} annotated on the type '{}'",
                        beanName,
                        attributes,
                        metadata.getClassName()
                );
            }
            return;
        }

        final BeanDefinition existedBeanDefinition = getApplicationEventMulticasterBeanDefinition(beanName, registry);

        final BeanDefinition targetBeanDefinition;

        if (existedBeanDefinition == null) {
            // NO ApplicationEventMulticaster BeanDefinition present
            targetBeanDefinition = buildApplicationEventMulticasterBeanDefinition(intercepted, executorForListener, metadata);
        } else {
            if (isSameBeanDefinition(existedBeanDefinition, intercepted, executorForListener)) {
                if (logger.isInfoEnabled()) {
                    logger.info("The same {} was annotated on the class '{}'", attributes, existedBeanDefinition.getAttribute(CLASS_NAME_ATTRIBUTE_NAME));
                }
                return;
            }
            // Current BeanFactory registered a BeanDefinition for ApplicationEventMulticaster
            targetBeanDefinition = rebuildApplicationEventMulticasterBeanDefinition(intercepted, existedBeanDefinition, beanName, registry);
        }

        associateExecutorBeanIfRequired(targetBeanDefinition, associatedExecutorBean, executorForListener);
        registry.registerBeanDefinition(beanName, targetBeanDefinition);
    }

    private boolean isSameBeanDefinition(BeanDefinition beanDefinition, boolean intercepted, String executorForListener) {
        if (!Objects.equals(intercepted, beanDefinition.getAttribute(INTERCEPTED_ATTRIBUTE_NAME))) {
            return false;
        }
        if (!Objects.equals(executorForListener, beanDefinition.getAttribute(EXECUTOR_FOR_LISTENER_ATTRIBUTE_NAME))) {
            return false;
        }
        return true;
    }

    BeanDefinition getApplicationEventMulticasterBeanDefinition(String beanName, BeanDefinitionRegistry registry) {
        return registry.containsBeanDefinition(beanName) ? registry.getBeanDefinition(beanName) : null;
    }

    private AbstractBeanDefinition rebuildApplicationEventMulticasterBeanDefinition(boolean intercepted,
                                                                                    BeanDefinition existedBeanDefinition,
                                                                                    String beanName,
                                                                                    BeanDefinitionRegistry registry) {

        final AbstractBeanDefinition targetBeanDefinition;
        if (intercepted) {
            // Remove the original BeanDefinition of ApplicationEventMulticaster
            registry.removeBeanDefinition(beanName);
            String resetBeanName = getResetBeanName(this.environment);
            // Reset bean name and re-register the original BeanDefinition of ApplicationEventMulticaster
            registry.registerBeanDefinition(resetBeanName, existedBeanDefinition);
            // Build BeanDefinition InterceptingApplicationEventMulticasterProxy with bean name
            targetBeanDefinition = new RootBeanDefinition(InterceptingApplicationEventMulticasterProxy.class);
        } else {
            targetBeanDefinition = (AbstractBeanDefinition) existedBeanDefinition;
        }

        return targetBeanDefinition;
    }

    private AbstractBeanDefinition buildApplicationEventMulticasterBeanDefinition(boolean intercepted, String executorForListener, AnnotationMetadata metadata) {
        Class<?> beanClass = intercepted ? InterceptingApplicationEventMulticaster.class : SimpleApplicationEventMulticaster.class;
        RootBeanDefinition beanDefinition = new RootBeanDefinition(beanClass);
        beanDefinition.setAttribute(INTERCEPTED_ATTRIBUTE_NAME, intercepted);
        beanDefinition.setAttribute(EXECUTOR_FOR_LISTENER_ATTRIBUTE_NAME, executorForListener);
        beanDefinition.setAttribute(CLASS_NAME_ATTRIBUTE_NAME, metadata.getClassName());
        return beanDefinition;
    }

    private void associateExecutorBeanIfRequired(BeanDefinition beanDefinition,
                                                 boolean associatedExecutorBean,
                                                 String executorBeanName) {
        if (associatedExecutorBean) {
            MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
            // The bean class must have setTaskExecutor(Executor) method
            propertyValues.addPropertyValue("taskExecutor", new RuntimeBeanReference(executorBeanName));
        }
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
