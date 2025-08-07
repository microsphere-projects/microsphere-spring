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
package io.microsphere.spring.beans.factory.config;

import io.microsphere.spring.test.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.findBeanNames;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.findInfrastructureBeanNames;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.genericBeanDefinition;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.isInfrastructureBean;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.resolveBeanType;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_APPLICATION;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * {@link BeanDefinitionUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see BeanDefinitionUtils
 * @since 1.0.0
 */
public class BeanDefinitionUtilsTest {

    private static final String USER_BEAN_NAME = "user";

    private static final String USERS_BEAN_NAME = "users";

    static final String BEAN_NAME = "config";

    private AbstractBeanDefinition beanDefinition;

    @BeforeEach
    public void setUp() {
        this.beanDefinition = genericBeanDefinition(User.class);
    }

    /**
     * Test methods:
     * <ul>
     *     <li>{@link BeanDefinitionUtils#genericBeanDefinition(Class)}</li>
     *     <li>{@link BeanDefinitionUtils#genericBeanDefinition(Class, int)}</li>
     *     <li>{@link BeanDefinitionUtils#genericBeanDefinition(Class, int, Object...)}</li>
     * </ul>
     */
    @Test
    public void testGenericBeanDefinition() {
        AbstractBeanDefinition beanDefinition = this.beanDefinition;
        assertBeanDefinition(beanDefinition, ROLE_APPLICATION);

        beanDefinition = genericBeanDefinition(User.class, ROLE_INFRASTRUCTURE);
        assertBeanDefinition(beanDefinition, ROLE_INFRASTRUCTURE);

        beanDefinition = genericBeanDefinition(User.class, "Mercy", 38);
        assertBeanDefinition(beanDefinition, ROLE_APPLICATION, "Mercy", 38);

        beanDefinition = genericBeanDefinition(User.class, ROLE_INFRASTRUCTURE, new Object[]{"Mercy", 38});
        assertBeanDefinition(beanDefinition, ROLE_INFRASTRUCTURE, "Mercy", 38);

    }

    @Test
    public void testResolveBeanType() {
        testInSpringContainer((context) -> {
            ConfigurableBeanFactory beanFactory = context.getBeanFactory();
            RootBeanDefinition beanDefinition = (RootBeanDefinition) beanFactory.getMergedBeanDefinition(USER_BEAN_NAME);
            assertEquals(User.class, resolveBeanType(beanDefinition, context.getClassLoader()));
            assertEquals(User.class, resolveBeanType(beanDefinition));

            beanDefinition = (RootBeanDefinition) beanFactory.getMergedBeanDefinition(USERS_BEAN_NAME);
            assertEquals(List.class, resolveBeanType(beanDefinition));

            beanDefinition = (RootBeanDefinition) beanFactory.getMergedBeanDefinition(BEAN_NAME);
            assertEquals(Config.class, resolveBeanType(beanDefinition));
        }, Config.class);
    }

    @Test
    public void testResolveBeanTypeOnFallback() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClassName(User.class.getName());
        assertEquals(User.class, resolveBeanType(beanDefinition));
    }

    @Test
    public void testFindInfrastructureBeanNames() {
        testInSpringContainer((context) -> {
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            Set<String> infrastructureBeanNames = findInfrastructureBeanNames(beanFactory);
            assertTrue(infrastructureBeanNames.contains(USER_BEAN_NAME));
            assertFalse(infrastructureBeanNames.contains(USERS_BEAN_NAME));
        }, Config.class);
    }

    @Test
    public void testFindBeanNames() {
        testInSpringContainer((context, beanFactory) -> {
            Set<String> beanNames = findBeanNames(context.getBeanFactory());
            assertTrue(beanNames.contains(USER_BEAN_NAME));
            assertTrue(beanNames.contains(USERS_BEAN_NAME));
        }, Config.class);
    }

    @Test
    public void testFindBeanNamesOnNullBeanFactory() {
        assertSame(emptySet(), findBeanNames(null));
    }

    @Test
    public void testIsInfrastructureBean() {
        AbstractBeanDefinition beanDefinition = this.beanDefinition;
        assertFalse(isInfrastructureBean(beanDefinition));

        beanDefinition.setRole(ROLE_INFRASTRUCTURE);
        assertTrue(isInfrastructureBean(beanDefinition));
    }

    @Test
    public void testIsInfrastructureBeanOnNull() {
        assertFalse(isInfrastructureBean(null));
    }

    private void assertBeanDefinition(AbstractBeanDefinition beanDefinition, int role, Object... constructorArguments) {
        ConstructorArgumentValues argumentValues = beanDefinition.getConstructorArgumentValues();
        assertEquals(role, beanDefinition.getRole());
        int length = constructorArguments.length;
        assertEquals(length, argumentValues.getArgumentCount());
        for (int i = 0; i < length; i++) {
            ConstructorArgumentValues.ValueHolder argumentValue = argumentValues.getArgumentValue(i, Object.class);
            assertEquals(constructorArguments[i], argumentValue.getValue());
        }
    }

    @Component(BEAN_NAME)
    static class Config {

        @Bean(name = USER_BEAN_NAME)
        @Role(ROLE_INFRASTRUCTURE)
        public User user() {
            return new User();
        }

        @Bean(name = USERS_BEAN_NAME)
        public List<User> users() {
            return asList(new User());
        }
    }
}
