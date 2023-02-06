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
package io.github.microsphere.spring.util;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static io.github.microsphere.spring.util.BeanRegistrar.*;
import static org.junit.Assert.*;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * {@link BeanRegistrar} Test
 *
 * @since 1.0.3
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

        for (int i = 0; i < 99999; i++) {
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
        assertEquals(2, registerSpringFactoriesBeans(registry, Bean.class));
        assertTrue(registry.containsBeanDefinition("testBean"));
        assertTrue(registry.containsBeanDefinition("testBean2"));
        assertEquals(TestBean.class,registry.getBean("testBean").getClass());
        assertEquals(TestBean2.class,registry.getBean("testBean2").getClass());
    }

}
