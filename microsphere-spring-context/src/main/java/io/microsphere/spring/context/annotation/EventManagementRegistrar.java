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

import io.microsphere.spring.context.event.InterceptingApplicationEventMulticaster;
import io.microsphere.spring.context.event.InterceptingApplicationEventMulticasterProxy;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.concurrent.Executor;

import static io.microsphere.spring.context.annotation.EnableEventManagement.NO_EXECUTOR;
import static io.microsphere.spring.context.event.InterceptingApplicationEventMulticasterProxy.getResetBeanName;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;
import static org.springframework.util.StringUtils.hasText;

/**
 * Event Management Registrar
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableEventManagement
 * @see InterceptingApplicationEventMulticaster
 * @see SimpleApplicationEventMulticaster
 * @see Executor
 * @see TaskExecutor
 * @since 1.0.0
 */
public class EventManagementRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private static final String ANNOTATION_CLASS_NAME = EnableEventManagement.class.getName();

    private static final String BEAN_NAME = APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ANNOTATION_CLASS_NAME);
        boolean intercepted = (Boolean) attributes.get("intercepted");
        String executorForListener = (String) attributes.get("executorForListener");
        registerApplicationEventMulticaster(intercepted, executorForListener, registry);
    }

    private void registerApplicationEventMulticaster(boolean intercepted,
                                                     String executorForListener,
                                                     BeanDefinitionRegistry registry) {
        if (!intercepted && NO_EXECUTOR.equals(executorForListener)) {
            return;
        }

        String beanName = BEAN_NAME;

        boolean associatedExecutorBean = !NO_EXECUTOR.equals(executorForListener);


        boolean beanExists = registry.containsBeanDefinition(beanName);

        final AbstractBeanDefinition targetBeanDefinition;

        if (beanExists) {
            // Current BeanFactory registered a BeanDefinition for ApplicationEventMulticaster
            targetBeanDefinition = rebuildApplicationEventMulticasterBeanDefinition(intercepted, beanName, registry);
        } else {
            // NO ApplicationEventMulticaster BeanDefinition present
            targetBeanDefinition = buildApplicationEventMulticasterBeanDefinition(intercepted);
        }

        associateExecutorBeanIfRequired(targetBeanDefinition, associatedExecutorBean, executorForListener);
        registry.registerBeanDefinition(beanName, targetBeanDefinition);
    }

    private AbstractBeanDefinition rebuildApplicationEventMulticasterBeanDefinition(boolean intercepted,
                                                                                    String beanName, BeanDefinitionRegistry registry) {

        BeanDefinition originalBeanDefinition = registry.getBeanDefinition(beanName);

        final AbstractBeanDefinition targetBeanDefinition;
        if (intercepted) {
            // Remove the original BeanDefinition of ApplicationEventMulticaster
            registry.removeBeanDefinition(beanName);
            String resetBeanName = getResetBeanName(this.environment);
            // Reset bean name and re-register the original BeanDefinition of ApplicationEventMulticaster
            registry.registerBeanDefinition(resetBeanName, originalBeanDefinition);
            // Build BeanDefinition InterceptingApplicationEventMulticasterProxy with bean name
            targetBeanDefinition = new RootBeanDefinition(InterceptingApplicationEventMulticasterProxy.class);
        } else {
            targetBeanDefinition = (AbstractBeanDefinition) originalBeanDefinition;
        }

        return targetBeanDefinition;
    }


    private AbstractBeanDefinition buildApplicationEventMulticasterBeanDefinition(boolean intercepted) {
        Class<?> beanClass = intercepted ? InterceptingApplicationEventMulticaster.class : SimpleApplicationEventMulticaster.class;
        return new RootBeanDefinition(beanClass);
    }

    private String getExecutorBeanName(boolean associatedExecutorBean, String executorForListener) {
        return associatedExecutorBean ? executorForListener : null;
    }

    private AbstractBeanDefinition buildBeanDefinition(Class<?> beanClass, @Nullable String executorBeanName) {
        BeanDefinitionBuilder beanDefinitionBuilder = genericBeanDefinition(beanClass);
        if (hasText(executorBeanName)) {

        }
        return beanDefinitionBuilder.getBeanDefinition();
    }

    private void associateExecutorBeanIfRequired(AbstractBeanDefinition beanDefinition,
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
