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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.EventListener;
import java.util.List;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.core.ResolvableType.forClass;

/**
 * Abstract {@link EventListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanFactoryListener
 * @see EventPublishingBeanInitializer
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        initializers = {
                EventPublishingBeanInitializer.class
        })
public abstract class AbstractEventListenerTest<L extends EventListener> {

    protected final Logger logger = getLogger(getClass());

    @Autowired
    protected L beanFactoryListener;

    @Test
    void test() {
        assertNotNull(beanFactoryListener);
    }

    static class Factory implements ContextCustomizerFactory {

        @Override
        public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
            Class<?> declaringClass = getClass().getDeclaringClass();
            if (declaringClass.isAssignableFrom(testClass)) {
                return (context, mergedConfig) -> {
                    Class<?> listenerClass = forClass(testClass)
                            .as(AbstractEventListenerTest.class)
                            .getGeneric(0)
                            .resolve(Object.class);
                    ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
                    BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                    registerBeanDefinition(registry, listenerClass);
                };
            }
            return null;
        }
    }
}