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
package io.microsphere.spring.beans.factory.support;

import io.microsphere.spring.test.Bean;
import io.microsphere.spring.test.TestBean;
import io.microsphere.spring.test.TestBean2;
import io.microsphere.spring.test.domain.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static io.microsphere.spring.beans.factory.support.BeanRegistrar.hasAlias;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerFactoryBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerInfrastructureBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerSingleton;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerSpringFactoriesBeans;
import static java.beans.Introspector.decapitalize;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
    public void setUp() {
        this.beanFactory = new DefaultListableBeanFactory();
    }

    @After
    public void destroy() {
        this.beanFactory.destroySingletons();
    }

    @Test
    public void testRegisterInfrastructureBean() {
        assertBeanDefinitions(() -> registerInfrastructureBean(beanFactory, User.class), true, ROLE_INFRASTRUCTURE, "io.microsphere.spring.test.domain.User#0");
        assertBeanDefinitions(() -> registerInfrastructureBean(beanFactory, User.class), true, ROLE_INFRASTRUCTURE, "io.microsphere.spring.test.domain.User#0", "io.microsphere.spring.test.domain.User#1");
    }

    @Test
    public void testRegisterBeanDefinition() {
        assertBeanDefinitions(() -> registerBeanDefinition(beanFactory, User.class), true, ROLE_APPLICATION, "io.microsphere.spring.test.domain.User#0");
        assertBeanDefinitions(() -> registerBeanDefinition(beanFactory, User.class), true, ROLE_APPLICATION, "io.microsphere.spring.test.domain.User#0", "io.microsphere.spring.test.domain.User#1");
    }

    @Test
    public void testRegisterSingleton() {
        registerUserAsSingleton();
    }

    @Test
    public void testHasAlias() {
        String beanName = registerUserAsSingleton();
        String alias = "test-user";
        assertFalse(hasAlias(beanFactory, beanName, alias));

        beanFactory.registerAlias(beanName, alias);
        assertTrue(hasAlias(beanFactory, beanName, alias));
    }

    @Test
    public void testRegisterSpringFactoriesBeans() {
        int beansCount = registerSpringFactoriesBeans((BeanFactory) this.beanFactory, Bean.class);
        assertEquals(2, beansCount);
        assertTrue(this.beanFactory.containsBean(decapitalize(TestBean.class.getSimpleName())));
        assertTrue(this.beanFactory.containsBean(decapitalize(TestBean2.class.getSimpleName())));
    }

    @Test
    public void testRegisterFactoryBean() {
        testRegisterBean((beanName, bean) -> registerFactoryBean(this.beanFactory, beanName, bean));
    }

    @Test
    public void testRegisterBean() {
        testRegisterBean((beanName, bean) -> registerBean(this.beanFactory, beanName, bean));
    }

    private void testRegisterBean(BiConsumer<String, Object> beanConsumer) {
        String beanName = "testBean";
        TestBean testBean = new TestBean();
        assertNull(testBean.getBeanName());
        beanConsumer.accept(beanName, testBean);
        TestBean bean = this.beanFactory.getBean(beanName, TestBean.class);
        assertEquals(beanName, bean.getBeanName());
        assertEquals(bean.getBeanName(), testBean.getBeanName());
        assertEquals(testBean, bean);
    }

    private String registerUserAsSingleton() {
        String beanName = "user";
        User user = new User();
        registerSingleton(beanFactory, beanName, user);
        assertEquals(user, beanFactory.getBean(beanName));
        return beanName;
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
