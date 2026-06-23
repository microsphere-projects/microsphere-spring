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
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.genericBeanDefinition;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.hasAlias;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerFactoryBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerGenericBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerGenericBeans;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerInfrastructureBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerSingleton;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerSpringFactoriesBeans;
import static java.beans.Introspector.decapitalize;
import static java.util.Collections.emptyMap;
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

    private DefaultListableBeanFactory defaultListableBeanFactory;

    private BeanFactory beanFactory;

    @BeforeEach
    void setUp() {
        this.defaultListableBeanFactory = new DefaultListableBeanFactory();
        this.beanFactory = defaultListableBeanFactory;
    }

    @AfterEach
    void tearDown() {
        this.defaultListableBeanFactory.destroySingletons();
    }

    @Test
    void testRegisterInfrastructureBean() {
        assertBeanDefinitions(() -> registerInfrastructureBean(this.beanFactory, User.class), true, ROLE_INFRASTRUCTURE, false, "io.microsphere.spring.test.domain.User#0");
        assertBeanDefinitions(() -> registerInfrastructureBean(asBeanDefinitionRegistry(this.beanFactory), User.class), true, ROLE_INFRASTRUCTURE, false, "io.microsphere.spring.test.domain.User#0", "io.microsphere.spring.test.domain.User#1");
    }

    @Test
    void testRegisterInfrastructureBeanWithBeanName() {
        String userName = "user";
        assertBeanDefinitions(() -> registerInfrastructureBean(this.beanFactory, userName, User.class), true, ROLE_INFRASTRUCTURE, false, userName);
    }

    @Test
    void testRegisterBeanDefinition() {
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, User.class), true, ROLE_APPLICATION, false, "io.microsphere.spring.test.domain.User#0");
        assertBeanDefinitions(() -> registerBeanDefinition(asBeanDefinitionRegistry(this.beanFactory), User.class), true, ROLE_APPLICATION, false, "io.microsphere.spring.test.domain.User#0", "io.microsphere.spring.test.domain.User#1");

        String beanName = "user";
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, beanName, User.class), true, ROLE_APPLICATION, false, "io.microsphere.spring.test.domain.User#0", "io.microsphere.spring.test.domain.User#1", beanName);
    }

    @Test
    void testRegisterBeanDefinitionWithConsumer() {
        String beanName = "user";
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, beanName, User.class, builder -> builder.setRole(ROLE_INFRASTRUCTURE)), true, ROLE_INFRASTRUCTURE, false, beanName);
    }

    @Test
    void testRegisterBeanDefinitionWithName() {
        String beanName = "user";
        BeanDefinition beanDefinition = genericBeanDefinition(User.class);
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, beanName, beanDefinition), true, ROLE_APPLICATION, false, beanName);

    }

    @Test
    void testRegisterBeanDefinitionWithConstructorArguments() {
        String beanName = "user";
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, beanName, User.class, "Mercy", 18), true, ROLE_APPLICATION, false, beanName);
        assertBeanDefinitions(() -> registerBeanDefinition(asBeanDefinitionRegistry(this.beanFactory), beanName, User.class, "Mercy", 18), false, ROLE_APPLICATION, false, beanName);
        User user = this.beanFactory.getBean(beanName, User.class);
        assertEquals("Mercy", user.getName());
        assertEquals(18, user.getAge());
    }

    @Test
    void testRegisterBeanDefinitionOnOverriding() {
        this.defaultListableBeanFactory.setAllowBeanDefinitionOverriding(true);
        String beanName = "user";
        AbstractBeanDefinition beanDefinition = genericBeanDefinition(User.class, builder -> {
            builder.setRole(ROLE_APPLICATION);
        });
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, beanName, beanDefinition, true), true, ROLE_APPLICATION, false, beanName);

        assertTrue(registerBeanDefinition(asBeanDefinitionRegistry(this.beanFactory), beanName, beanDefinition, true));

        this.defaultListableBeanFactory.setAllowBeanDefinitionOverriding(false);
        assertFalse(registerBeanDefinition(this.beanFactory, beanName, beanDefinition, true));
    }

    @Test
    void testRegisterBeanDefinitionWithRole() {
        String beanName = "user";
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, beanName, User.class, ROLE_INFRASTRUCTURE), true, ROLE_INFRASTRUCTURE, false, beanName);
        assertBeanDefinitions(() -> registerBeanDefinition(asBeanDefinitionRegistry(this.beanFactory), beanName, User.class, ROLE_INFRASTRUCTURE), false, ROLE_INFRASTRUCTURE, false, beanName);
    }

    @Test
    void testRegisterBeanDefinitionWithPrimary() {
        String beanName = "user";
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, beanName, User.class, true), true, ROLE_APPLICATION, true, beanName);
        assertBeanDefinitions(() -> registerBeanDefinition(asBeanDefinitionRegistry(this.beanFactory), beanName, User.class, true), false, ROLE_APPLICATION, true, beanName);
    }

    @Test
    void testRegisterBeanDefinitionWithPrimaryAndNoBeanName() {
        String beanName = "io.microsphere.spring.test.domain.User#0";
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, User.class, true), true, ROLE_APPLICATION, true, beanName);
    }

    @Test
    void testRegisterBeanDefinitionWithBuilder() {
        String beanName = "io.microsphere.spring.test.domain.User#0";
        assertBeanDefinitions(() -> registerBeanDefinition(this.beanFactory, User.class, builder -> builder.setRole(ROLE_INFRASTRUCTURE)), true, ROLE_INFRASTRUCTURE, false, beanName);
    }

    @Test
    void testRegisterBeanDefinitionWithBeanDefinition() {
        BeanDefinition beanDefinition = genericBeanDefinition(User.class);
        assertTrue(registerBeanDefinition(this.beanFactory, beanDefinition));
        assertTrue(registerBeanDefinition(asBeanDefinitionRegistry(this.beanFactory), beanDefinition));
    }

    @Test
    void testRegisterSingleton() {
        registerUserAsSingleton();
    }

    @Test
    void testHasAlias() {
        assertFalse(hasAlias(this.defaultListableBeanFactory, null, null));
        String beanName = registerUserAsSingleton();
        String alias = "test-user";
        assertFalse(hasAlias(this.defaultListableBeanFactory, beanName, null));
        assertFalse(hasAlias(this.defaultListableBeanFactory, null, alias));
        assertFalse(hasAlias(this.defaultListableBeanFactory, beanName, alias));

        this.defaultListableBeanFactory.registerAlias(beanName, alias);
        assertTrue(hasAlias(defaultListableBeanFactory, beanName, alias));
    }

    @Test
    void testRegisterSpringFactoriesBeans() {
        Map<Class, String> classStringMap = registerSpringFactoriesBeans(this.beanFactory, Bean.class);
        assertEquals(2, classStringMap.size());
        classStringMap = registerSpringFactoriesBeans(this.beanFactory, Bean.class);
        assertEquals(2, classStringMap.size());

        assertTrue(this.beanFactory.containsBean(decapitalize(TestBean.class.getSimpleName())));
        assertTrue(this.beanFactory.containsBean(decapitalize(TestBean2.class.getSimpleName())));
    }

    @Test
    void testRegisterFactoryBean() {
        testRegisterBean((beanName, bean) -> registerFactoryBean(this.beanFactory, beanName, bean));
    }

    @Test
    void testRegisterFactoryBeanWithPrimary() {
        testRegisterBean((beanName, bean) -> registerFactoryBean(this.beanFactory, beanName, bean, true));
    }

    @Test
    void testRegisterBean() {
        testRegisterBean((beanName, bean) -> registerBean(this.beanFactory, beanName, bean));
    }

    @Test
    void testRegisterBeanWithPrimary() {
        testRegisterBean((beanName, bean) -> registerBean(this.beanFactory, beanName, bean, true));
    }

    @Test
    void testRegisterGenericBeans() {
        String beanName = "user";
        Map<Class<?>, String> beanTypesAndNames = registerGenericBeans(this.beanFactory, User.class);
        assertEquals(1, beanTypesAndNames.size());
        assertTrue(beanTypesAndNames.containsKey(User.class));
        assertEquals(beanName, beanTypesAndNames.get(User.class));
    }

    @Test
    void testRegisterGenericBeansWithCollection() {
        String beanName = "user";
        Map<Class<?>, String> beanTypesAndNames = registerGenericBeans(this.beanFactory, ofList(User.class));
        assertEquals(1, beanTypesAndNames.size());
        assertTrue(beanTypesAndNames.containsKey(User.class));
        assertEquals(beanName, beanTypesAndNames.get(User.class));
    }

    @Test
    void testRegisterGenericBeansOnEmptyBeanClasses() {
        assertEquals(emptyMap(), registerGenericBeans(this.beanFactory));
    }

    @Test
    void testRegisterGenericBean() {
        String beanName = "user";
        Entry<String, Boolean> beanNameAndRegistered = registerGenericBean(this.beanFactory, User.class);
        assertEquals(beanName, beanNameAndRegistered.getKey());
        assertTrue(beanNameAndRegistered.getValue());

        beanNameAndRegistered = registerGenericBean(asBeanDefinitionRegistry(this.beanFactory), User.class);
        assertEquals(beanName, beanNameAndRegistered.getKey());
        assertFalse(beanNameAndRegistered.getValue());
    }

    @Test
    void testRegisterGenericBeanWithBeanNameGenerator() {
        AnnotationBeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

        String beanName = beanNameGenerator.generateBeanName(genericBeanDefinition(User.class), asBeanDefinitionRegistry(this.beanFactory));
        Entry<String, Boolean> beanNameAndRegistered = registerGenericBean(this.beanFactory, User.class, beanNameGenerator);
        assertEquals(beanName, beanNameAndRegistered.getKey());
        assertTrue(beanNameAndRegistered.getValue());

        beanNameAndRegistered = registerGenericBean(asBeanDefinitionRegistry(this.beanFactory), User.class, beanNameGenerator);
        assertEquals(beanName, beanNameAndRegistered.getKey());
        assertFalse(beanNameAndRegistered.getValue());
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

    private void testRegisterBeanWithBeanFactoryHelper(BiConsumer<String, Object> beanConsumer) {
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
        registerSingleton((BeanFactory) null, beanName, user);
        assertEquals(user, this.beanFactory.getBean(beanName));
        return beanName;
    }

    private void assertBeanDefinitions(Supplier<Boolean> booleanSupplier, boolean expectedRegistered, int role,
                                       boolean primary, String... expectedBeanNames) {
        boolean registered = booleanSupplier.get();

        String[] beanNames = this.defaultListableBeanFactory.getBeanNamesForType(User.class);

        assertEquals(expectedRegistered, registered);
        assertArrayEquals(expectedBeanNames, beanNames);
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = this.defaultListableBeanFactory.getBeanDefinition(beanName);
            assertEquals(role, beanDefinition.getRole());
            assertEquals(primary, beanDefinition.isPrimary());
        }
    }
}