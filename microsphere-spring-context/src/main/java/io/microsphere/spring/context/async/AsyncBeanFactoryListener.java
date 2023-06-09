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
package io.microsphere.spring.context.async;

import io.microsphere.spring.context.event.BeanFactoryListener;
import io.microsphere.spring.context.event.BeanFactoryListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.lang.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import static io.microsphere.util.ClassUtils.resolveClass;
import static java.util.Collections.emptyList;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Async {@link BeanFactoryListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class AsyncBeanFactoryListener extends BeanFactoryListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AsyncBeanFactoryListener.class);

    @Override
    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory beanFactory) {
        String[] beanNames = beanFactory.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            if (beanFactory.containsSingleton(beanName)) {
                logger.info("The Bean[name : '{}'] is ready", beanName);
            } else {
                BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
                logger.info("The Bean[name : '{}'] Definition : {}", beanName, beanDefinition.getClass());

                if (isNonLazyInitSingleton(beanDefinition) && beanDefinition instanceof RootBeanDefinition) {
                    // Not Ready Bean
                    RootBeanDefinition mergedBeanDefinition = (RootBeanDefinition) beanFactory.getMergedBeanDefinition(beanName);
                    ClassLoader classLoader = beanFactory.getBeanClassLoader();
                    Method factoryMethod = mergedBeanDefinition.getResolvedFactoryMethod();
                    if (factoryMethod == null) { // The bean-class Definition
                        Class<?> beanClass = getBeanClass(mergedBeanDefinition, classLoader);

                        Constructor[] constructors = findConstructors(beanName, beanClass, beanFactory);
                        int constructorsLength = constructors.length;
                        if (constructorsLength != 1) {
                            logger.warn("Why the Bean[name : '{}' , class : {} ] has {} constructors?", beanName, beanClass, constructorsLength);
                            continue;
                        }
                        // TODO
                        Constructor constructor = constructors[0];

                    } else { // the @Bean or customized Method Definition
                        Type[] genericParameterTypes = factoryMethod.getGenericParameterTypes();
                        // TODO
                    }
                    if (mergedBeanDefinition instanceof AnnotatedBeanDefinition) {
                        AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
                        annotatedBeanDefinition.getFactoryMethodMetadata();
                    } else {

                    }
                }
            }
        }
    }

    private Constructor[] findConstructors(String beanName, Class<?> beanClass, ConfigurableListableBeanFactory beanFactory) {
        List<SmartInstantiationAwareBeanPostProcessor> processors = getSmartInstantiationAwareBeanPostProcessors(beanFactory);
        Constructor[] constructors = null;
        for (SmartInstantiationAwareBeanPostProcessor processor : processors) {
            constructors = processor.determineCandidateConstructors(beanClass, beanName);
            if (constructors != null) {
                break;
            }
        }
        return isEmpty(constructors) ? beanClass.getConstructors() : constructors;
    }

    private List<SmartInstantiationAwareBeanPostProcessor> getSmartInstantiationAwareBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        if (beanFactory instanceof DefaultListableBeanFactory) {
            DefaultListableBeanFactory dbf = (DefaultListableBeanFactory) beanFactory;
            List<SmartInstantiationAwareBeanPostProcessor> processors = new LinkedList<>();
            List<BeanPostProcessor> beanPostProcessors = dbf.getBeanPostProcessors();
            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                if (beanPostProcessor instanceof SmartInstantiationAwareBeanPostProcessor) {
                    processors.add((SmartInstantiationAwareBeanPostProcessor) beanPostProcessor);
                }
            }
            return processors;
        } else {
            return emptyList();
        }

    }

    private Class<?> getBeanClass(RootBeanDefinition beanDefinition, @Nullable ClassLoader classLoader) {
        return beanDefinition.hasBeanClass() ? beanDefinition.getBeanClass() :
                resolveClass(beanDefinition.getBeanClassName(), classLoader);
    }

    private boolean isNonLazyInitSingleton(BeanDefinition beanDefinition) {
        return beanDefinition != null && beanDefinition.isSingleton() && !beanDefinition.isLazyInit();
    }
}
