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


import io.microsphere.spring.test.web.controller.TestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link ExposingClassPathBeanDefinitionScanner} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ExposingClassPathBeanDefinitionScanner
 * @since 1.0.0
 */
class ExposingClassPathBeanDefinitionScannerTest {

    private ConfigurableApplicationContext context;

    private DefaultListableBeanFactory beanFactory;

    private Environment environment;

    private ExposingClassPathBeanDefinitionScanner scanner;

    @BeforeEach
    void setUp() {
        this.context = new AnnotationConfigApplicationContext();
        this.beanFactory = (DefaultListableBeanFactory) this.context.getBeanFactory();
        this.environment = this.context.getEnvironment();
        this.scanner = new ExposingClassPathBeanDefinitionScanner(this.beanFactory, true, this.environment, this.context);
    }

    @Test
    void testScanAndCheck() {
        assertScanAndCheck(false);
    }

    @Test
    void testScanAndRegister() {
        assertScanAndCheck(true);
    }

    @Test
    void testGetSingletonBeanRegistry() {
        assertSame(this.beanFactory, this.scanner.getSingletonBeanRegistry());
    }

    @Test
    void testRegisterSingleton() {
        this.scanner.registerSingleton("test", this);
        assertSame(this, this.beanFactory.getSingleton("test"));
    }

    void assertScanAndCheck(boolean registered) {
        String packageName = TestController.class.getPackage().getName();
        Set<BeanDefinitionHolder> beanDefinitionHolders = this.scanner.doScan(packageName);
        assertEquals(1, beanDefinitionHolders.size());
        for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
            String beanName = beanDefinitionHolder.getBeanName();
            BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
            if (registered) {
                this.scanner.registerBeanDefinition(beanName, beanDefinition);
            } else {
                assertFalse(this.scanner.checkCandidate(beanName, beanDefinition));
            }
        }
    }
}