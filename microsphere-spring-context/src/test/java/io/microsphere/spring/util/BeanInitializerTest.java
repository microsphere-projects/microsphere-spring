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
package io.microsphere.spring.util;

import io.microsphere.spring.beans.factory.support.BeanRegistrar;
import io.microsphere.spring.test.Bean;
import io.microsphere.spring.test.TestBean;
import io.microsphere.spring.test.TestBean2;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static io.microsphere.spring.beans.factory.support.BeanRegistrar.hasAlias;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerInfrastructureBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerSpringFactoriesBeans;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * {@link BeanRegistrar} Test
 *
 * @since 1.0.0
 */
public class BeanInitializerTest {

    private static final String BEAN_NAME = "testBean";

    private DefaultListableBeanFactory registry;

    @Before
    public void init() {
        registry = new DefaultListableBeanFactory();
    }

    @Test
    public void testRegisterInfrastructureBean() {

        assertTrue(registerInfrastructureBean(registry, BEAN_NAME, TestBean.class));

        BeanDefinition beanDefinition = registry.getBeanDefinition(BEAN_NAME);

        assertEquals(ROLE_INFRASTRUCTURE, beanDefinition.getRole());

        for (int i = 0; i < 9; i++) {
            assertFalse(registerInfrastructureBean(registry, BEAN_NAME, TestBean.class));
        }
    }

    @Test
    public void testHasAlias() {
        testRegisterInfrastructureBean();
        registry.registerAlias(BEAN_NAME, "A");
        assertTrue(hasAlias(registry, BEAN_NAME, "A"));
    }

    @Test
    public void testRegisterSpringFactoriesBeans() {
        assertEquals(2, registerSpringFactoriesBeans((BeanDefinitionRegistry) registry, Bean.class));
        assertTrue(registry.containsBeanDefinition("testBean"));
        assertTrue(registry.containsBeanDefinition("testBean2"));
        assertEquals(TestBean.class, registry.getBean("testBean").getClass());
        assertEquals(TestBean2.class, registry.getBean("testBean2").getClass());
    }

}
