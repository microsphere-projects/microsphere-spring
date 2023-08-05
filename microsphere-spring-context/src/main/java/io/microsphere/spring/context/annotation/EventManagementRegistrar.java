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
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.concurrent.Executor;

import static io.microsphere.spring.context.annotation.EnableEventManagement.NO_EXECUTOR;
import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

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
public class EventManagementRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String ANNOTATION_CLASS_NAME = EnableEventManagement.class.getName();

    private static final String BEAN_NAME = APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

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

        if (registry.containsBeanDefinition(BEAN_NAME)) {
            // Current BeanFactory registered a BeanDefinition for ApplicationEventMulticaster
            registerApplicationEventMulticasterIfPresent(intercepted, executorForListener, registry);
        } else {
            // NO ApplicationEventMulticaster BeanDefinition present
            registerApplicationEventMulticasterIfAbsent(intercepted, executorForListener, registry);
        }
    }

    private void registerApplicationEventMulticasterIfPresent(boolean intercepted, String executorForListener,
                                                              BeanDefinitionRegistry registry) {
        // TODO
    }


    private void registerApplicationEventMulticasterIfAbsent(boolean intercepted, String executorForListener,
                                                             BeanDefinitionRegistry registry) {
        // TODO
    }

}
