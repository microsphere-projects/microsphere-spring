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

package io.microsphere.spring.beans;

import io.microsphere.spring.test.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.spring.beans.BeanSource.BEAN_FACTORY;
import static io.microsphere.spring.beans.BeanSource.JAVA_SERVICE_PROVIDER;
import static io.microsphere.spring.beans.BeanSource.SPRING_FACTORIES;
import static io.microsphere.spring.beans.BeanSource.registerBeans;
import static io.microsphere.spring.beans.BeanSource.values;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link BeanSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanSource
 * @since 1.0.0
 */
class BeanSourceTest {

    @Test
    void test() {
        BeanSource[] values = values();
        assertEquals(3, values.length);
        assertEquals(BEAN_FACTORY, values[0]);
        assertEquals(SPRING_FACTORIES, values[1]);
        assertEquals(JAVA_SERVICE_PROVIDER, values[2]);
    }

    @Test
    void testGetBeanTypes() {
        testInSpringContainer(context -> {
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            assertBeanTypes(beanFactory, BEAN_FACTORY);
            assertBeanTypes(beanFactory, SPRING_FACTORIES);
            assertBeanTypes(beanFactory, JAVA_SERVICE_PROVIDER);
        }, User.class);
    }

    @Test
    void testRegisterBeans() {
        assertRegisterBeans(BEAN_FACTORY);
        assertRegisterBeans(SPRING_FACTORIES);
        assertRegisterBeans(JAVA_SERVICE_PROVIDER);
    }

    @Test
    void testRegisterBeansOnStatic() {
        testInSpringContainer(context -> {
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            Map<Class<?>, String> beanTypesAndNames = registerBeans(beanFactory, ofArray(BEAN_FACTORY), User.class);
            assertBeans(beanTypesAndNames);

            beanTypesAndNames = registerBeans(beanFactory, ofArray(BEAN_FACTORY, SPRING_FACTORIES), User.class);
            assertBeans(beanTypesAndNames);

            beanTypesAndNames = registerBeans(beanFactory, ofArray(BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER), User.class);
            assertBeans(beanTypesAndNames);

            beanTypesAndNames = registerBeans(beanFactory, values());
            assertSame(emptyMap(), beanTypesAndNames);
        }, User.class);
    }

    void assertBeanTypes(ConfigurableListableBeanFactory beanFactory, BeanSource beanSource) {
        Set<Class<User>> beanTypes = beanSource.getBeanTypes(beanFactory, User.class);
        assertEquals(1, beanTypes.size());
        assertEquals(ofSet(User.class), beanTypes);
    }

    void assertRegisterBeans(BeanSource beanSource) {
        testInSpringContainer(context -> {
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            assertRegisterBeans(beanFactory, beanSource);
        }, User.class);
    }

    void assertRegisterBeans(ConfigurableListableBeanFactory beanFactory, BeanSource beanSource) {
        Map<Class<?>, String> beanTypesAndNames = beanSource.registerBeans(beanFactory, User.class);
        assertBeans(beanTypesAndNames);

        beanTypesAndNames = beanSource.registerBeans(beanFactory);
        assertSame(emptyMap(), beanTypesAndNames);
    }

    void assertBeans(Map<Class<?>, String> beanTypesAndNames) {
        assertEquals(1, beanTypesAndNames.size());
        assertEquals("user", beanTypesAndNames.get(User.class));
    }
}