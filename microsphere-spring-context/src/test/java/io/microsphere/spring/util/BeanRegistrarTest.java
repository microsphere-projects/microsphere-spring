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

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.function.Supplier;

import static io.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.util.BeanRegistrar.registerInfrastructureBean;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_APPLICATION;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * {@link BeanRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see BeanRegistrar
 * @since 1.0.0
 */
public class BeanRegistrarTest {

    private DefaultListableBeanFactory beanFactory;

    @Before
    public void before() {
        beanFactory = new DefaultListableBeanFactory();
    }

    @Test
    public void testRegisterInfrastructureBean() {
        assertBeanDefinitions(() -> registerInfrastructureBean(beanFactory, User.class), true, ROLE_INFRASTRUCTURE, "io.microsphere.spring.util.User#0");
        assertBeanDefinitions(() -> registerInfrastructureBean(beanFactory, User.class), true, ROLE_INFRASTRUCTURE, "io.microsphere.spring.util.User#0", "io.microsphere.spring.util.User#1");
    }

    @Test
    public void testRegisterBeanDefinition() {
        assertBeanDefinitions(() -> registerBeanDefinition(beanFactory, User.class), true, ROLE_APPLICATION, "io.microsphere.spring.util.User#0");
        assertBeanDefinitions(() -> registerBeanDefinition(beanFactory, User.class), true, ROLE_APPLICATION, "io.microsphere.spring.util.User#0", "io.microsphere.spring.util.User#1");
    }

    private void assertBeanDefinitions(Supplier<Boolean> booleanSupplier, boolean expectedRegistered, int role, String... expectedBeanNames) {
        boolean registered = booleanSupplier.get();

        String[] beanNames = beanFactory.getBeanNamesForType(User.class);

        assertEquals(expectedRegistered, registered);
        assertArrayEquals(expectedBeanNames, beanNames);
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            assertEquals(role, beanDefinition.getRole());
        }
    }
}
