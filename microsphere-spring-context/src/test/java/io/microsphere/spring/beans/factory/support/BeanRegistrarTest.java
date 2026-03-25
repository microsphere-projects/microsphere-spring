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

import io.microsphere.spring.beans.test.Bean;
import io.microsphere.spring.beans.test.TestBean;
import io.microsphere.spring.beans.test.TestBean2;
import io.microsphere.spring.test.domain.User;
import io.microsphere.spring.test.junit.jupiter.SpringLoggingTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.genericBeanDefinition;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.hasAlias;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerFactoryBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerInfrastructureBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerSingleton;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerSpringFactoriesBeans;
import static java.beans.Introspector.decapitalize;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_APPLICATION;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * {@link BeanRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see BeanRegistrar
 * @since 1.0.0
 */
@SpringLoggingTest
class BeanRegistrarTest {

    private DefaultListableBeanFactory beanFactory;

    @BeforeEach
    void setUp() {
        this.beanFactory = new DefaultListableBeanFactory();
    }

    @AfterEach
    void tearDown() {
        this.beanFactory.destroySingletons();
    }

    @Test
    void testRegisterInfrastructureBean() {
        assertBeanDefinitions(() -> registerInfrastructureBean(this.beanFactory, User.class), true, ROLE_INFRASTRUCTURE, "io.microsphere.spring.test.domain.User#0");
        assertBeanDefinitions(() -> registerInfrastructureBean(this.beanFactory, User.class), true, ROLE_INFRASTRUCTURE, "io.microsphere.spring.test.domain.User#0", "io.microsphere.spring.test.domain.User#1");
    }

    @Test
    void testRegisterBeanDefinition() {
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, User.class), true, ROLE_APPLICATION, "io.microsphere.spring.test.domain.User#0");
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, User.class), true, ROLE_APPLICATION, "io.microsphere.spring.test.domain.User#0", "io.microsphere.spring.test.domain.User#1");

        String beanName = "user";
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, beanName, User.class), true, ROLE_APPLICATION, "io.microsphere.spring.test.domain.User#0", "io.microsphere.spring.test.domain.User#1", beanName);
    }

    @Test
    void testRegisterBeanDefinitionWithConstructorArguments() {
        String beanName = "user";
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, beanName, User.class, "Mercy", 18), true, ROLE_APPLICATION, beanName);
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, beanName, User.class, "Mercy", 18), false, ROLE_APPLICATION, beanName);
        User user = this.beanFactory.getBean(beanName, User.class);
        assertEquals("Mercy", user.getName());
        assertEquals(18, user.getAge());
    }

    @Test
    void testRegisterBeanDefinitionOnOverriding() {
        this.beanFactory.setAllowBeanDefinitionOverriding(true);
        String beanName = "user";
        AbstractBeanDefinition beanDefinition = genericBeanDefinition(User.class, builder -> {
            builder.setRole(ROLE_APPLICATION);
        });
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, beanName, beanDefinition, true), true, ROLE_APPLICATION, beanName);

        assertTrue(registerBeanDefinition(this.beanFactory, beanName, beanDefinition, true));

        this.beanFactory.setAllowBeanDefinitionOverriding(false);
        assertFalse(registerBeanDefinition(this.beanFactory, beanName, beanDefinition, true));
    }

    @Test
    void testRegisterBeanDefinitionWithRole() {
        String beanName = "user";
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, beanName, User.class, ROLE_INFRASTRUCTURE), true, ROLE_INFRASTRUCTURE, beanName);
    }

    @Test
    void testRegisterSingleton() {
        registerUserAsSingleton();
    }

    @Test
    void testHasAlias() {
        assertFalse(hasAlias(this.beanFactory, null, null));
        String beanName = registerUserAsSingleton();
        String alias = "test-user";
        assertFalse(hasAlias(this.beanFactory, beanName, null));
        assertFalse(hasAlias(this.beanFactory, null, alias));
        assertFalse(hasAlias(this.beanFactory, beanName, alias));

        this.beanFactory.registerAlias(beanName, alias);
        assertTrue(hasAlias(beanFactory, beanName, alias));
    }

    @Test
    void testRegisterSpringFactoriesBeans() {
        int beansCount = registerSpringFactoriesBeans((BeanFactory) this.beanFactory, Bean.class);
        assertEquals(2, beansCount);
        assertEquals(0, registerSpringFactoriesBeans((BeanFactory) this.beanFactory, Bean.class));

        assertTrue(this.beanFactory.containsBean(decapitalize(TestBean.class.getSimpleName())));
        assertTrue(this.beanFactory.containsBean(decapitalize(TestBean2.class.getSimpleName())));
    }

    @Test
    void testRegisterFactoryBean() {
        testRegisterBean((beanName, bean) -> registerFactoryBean(this.beanFactory, beanName, bean));
    }

    @Test
    void testRegisterBean() {
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
        registerSingleton(this.beanFactory, beanName, user);
        assertEquals(user, this.beanFactory.getBean(beanName));
        return beanName;
    }

    private void assertBeanDefinitions(Supplier<Boolean> booleanSupplier, boolean expectedRegistered, int role, String... expectedBeanNames) {
        boolean registered = booleanSupplier.get();

        String[] beanNames = this.beanFactory.getBeanNamesForType(User.class);

        assertEquals(expectedRegistered, registered);
        assertArrayEquals(expectedBeanNames, beanNames);
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = this.beanFactory.getBeanDefinition(beanName);
            assertEquals(role, beanDefinition.getRole());
        }
    }
}