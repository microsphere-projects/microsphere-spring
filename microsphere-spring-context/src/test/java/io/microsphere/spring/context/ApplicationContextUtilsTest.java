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
package io.microsphere.spring.context;

import io.microsphere.spring.test.junit.jupiter.SpringLoggingTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ResourceLoader;

import java.util.List;

import static io.microsphere.spring.context.ApplicationContextUtils.APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS;
import static io.microsphere.spring.context.ApplicationContextUtils.asApplicationContext;
import static io.microsphere.spring.context.ApplicationContextUtils.asConfigurableApplicationContext;
import static io.microsphere.spring.context.ApplicationContextUtils.getApplicationContextAwareProcessor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link ApplicationContextUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ApplicationContextUtils
 * @since 1.0.0
 */
@SpringLoggingTest
class ApplicationContextUtilsTest {

    private GenericApplicationContext context;

    @BeforeEach
    void setUp() {
        this.context = new GenericApplicationContext();
        this.context.refresh();
    }

    @AfterEach
    void tearDown() {
        this.context.close();
    }

    @Test
    void testAsConfigurableApplicationContextWithContext() {
        ConfigurableApplicationContext applicationContext = asConfigurableApplicationContext(this.context);
        assertSame(this.context, applicationContext);
    }

    @Test
    void testAsConfigurableApplicationContextWithObject() {
        ResourceLoader resourceLoader = this.context;
        assertSame(this.context, asConfigurableApplicationContext(resourceLoader));
    }

    @Test
    void testAsApplicationContext() {
        ApplicationContext applicationContext = asApplicationContext(this.context);
        assertSame(this.context, applicationContext);
    }

    @Test
    void testGetApplicationContextAwareProcessor() {
        BeanPostProcessor beanPostProcessor = getApplicationContextAwareProcessor(this.context);
        assertEquals(APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS, beanPostProcessor.getClass());

        ConfigurableListableBeanFactory beanFactory = this.context.getBeanFactory();
        beanPostProcessor = getApplicationContextAwareProcessor(beanFactory);
        assertEquals(APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS, beanPostProcessor.getClass());

        DefaultListableBeanFactory defaultListableBeanFactory = new DefaultListableBeanFactory();
        assertNull(getApplicationContextAwareProcessor(defaultListableBeanFactory));

        defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        List<BeanPostProcessor> beanPostProcessors = defaultListableBeanFactory.getBeanPostProcessors();
        beanPostProcessor = getApplicationContextAwareProcessor(beanFactory);
        beanPostProcessors.remove(beanPostProcessor);
        assertNull(getApplicationContextAwareProcessor(beanFactory));
    }

    @Test
    void testGetApplicationContextAwareProcessorWithNullBeanFactory() {
        assertNull(getApplicationContextAwareProcessor((BeanFactory) null));
    }
}
