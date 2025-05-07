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

import io.microsphere.spring.util.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.findInfrastructureBeanNames;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.genericBeanDefinition;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.getInstanceSupplier;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.getResolvableType;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.isGetInstanceSupplierMethodPresent;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.isGetResolvableTypeMethodPresent;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.isInfrastructureBean;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.isSetInstanceSupplierMethodPresent;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.resolveBeanType;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.setInstanceSupplier;
import static io.microsphere.spring.core.SpringVersion.CURRENT;
import static io.microsphere.spring.core.SpringVersion.SPRING_5_0;
import static io.microsphere.spring.core.SpringVersion.SPRING_5_1;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_APPLICATION;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import static org.springframework.core.ResolvableType.NONE;

/**
 * {@link BeanDefinitionUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see BeanDefinitionUtils
 * @since 1.0.0
 */
public class BeanDefinitionUtilsTest {

    private static final boolean isGESpring5 = CURRENT.gt(SPRING_5_0);

    private static final boolean isGESpring5_1 = CURRENT.gt(SPRING_5_1);

    private static final String USER_BEAN_NAME = "user";

    private static final String USERS_BEAN_NAME = "users";

    static final String BEAN_NAME = "config";

    private AbstractBeanDefinition beanDefinition;

    @Before
    public void init() {
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
        testInSpringContainer((context, beanFactory) -> {
            RootBeanDefinition beanDefinition = (RootBeanDefinition) beanFactory.getMergedBeanDefinition(USER_BEAN_NAME);
            assertEquals(User.class, resolveBeanType(beanDefinition, context.getClassLoader()));
            assertEquals(User.class, resolveBeanType(beanDefinition));

            beanDefinition = (RootBeanDefinition) beanFactory.getMergedBeanDefinition(USERS_BEAN_NAME);
            assertEquals(List.class, resolveBeanType(beanDefinition));

            beanDefinition = (RootBeanDefinition) beanFactory.getMergedBeanDefinition(BEAN_NAME);
            assertEquals(Config.class, resolveBeanType(beanDefinition));
        });
    }

    @Test
    public void testResolveBeanTypeOnFallback() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClassName(User.class.getName());
        assertEquals(User.class, resolveBeanType(beanDefinition));
    }

    @Test
    public void testFindInfrastructureBeanNames() {
        testInSpringContainer((context, beanFactory) -> {
            Set<String> infrastructureBeanNames = findInfrastructureBeanNames(beanFactory);
            assertTrue(infrastructureBeanNames.contains(USER_BEAN_NAME));
            assertFalse(infrastructureBeanNames.contains(USERS_BEAN_NAME));
        });
    }

    @Test
    public void testGetResolvableType() {
        AbstractBeanDefinition beanDefinition = new RootBeanDefinition(User.class);
        assertEquals(User.class, getResolvableType(beanDefinition).resolve());

        beanDefinition = new RootBeanDefinition();
        assertNull(getResolvableType(beanDefinition).resolve());

        beanDefinition = genericBeanDefinition(User.class);
        assertEquals(User.class, getResolvableType(beanDefinition).resolve());

        beanDefinition = genericBeanDefinition(null);
        assertNull(getResolvableType(beanDefinition).resolve());
    }

    @Test
    public void testGetResolvableTypeOnNull() {
        RootBeanDefinition rootBeanDefinition = null;
        assertSame(NONE, getResolvableType(rootBeanDefinition));
    }

    @Test
    public void testGetResolvableTypeWithRootDefinition() {
        testInSpringContainer((context, beanFactory) -> {
            RootBeanDefinition beanDefinition = (RootBeanDefinition) beanFactory.getMergedBeanDefinition(USER_BEAN_NAME);
            ResolvableType resolvableType = getResolvableType(beanDefinition);
            assertEquals(User.class, resolvableType.getRawClass());

            beanDefinition = (RootBeanDefinition) beanFactory.getMergedBeanDefinition(USERS_BEAN_NAME);
            resolvableType = getResolvableType(beanDefinition);
            assertEquals(List.class, resolvableType.getRawClass());
            assertEquals(User.class, resolvableType.getGeneric(0).getRawClass());

            beanDefinition = (RootBeanDefinition) beanFactory.getMergedBeanDefinition(BEAN_NAME);
            resolvableType = getResolvableType(beanDefinition);
            assertEquals(Config.class, resolvableType.getRawClass());
        });
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

    @Test
    public void testMethodsPresent() {
        assertEquals(isGESpring5, isSetInstanceSupplierMethodPresent());
        assertEquals(isGESpring5, isGetInstanceSupplierMethodPresent());

        assertEquals(isGESpring5_1, isGetResolvableTypeMethodPresent());
    }

    @Test
    public void testSetAndGetInstanceSupplier() {
        AbstractBeanDefinition beanDefinition = this.beanDefinition;

        User user = new User();

        // setInstanceSupplier
        assertFalse(setInstanceSupplier(beanDefinition, null));
        assertEquals(isGESpring5, setInstanceSupplier(beanDefinition, () -> null));
        assertEquals(isGESpring5, setInstanceSupplier(beanDefinition, () -> user));

        // getInstanceSupplier
        Supplier<?> instanceSupplier = getInstanceSupplier(beanDefinition);
        Object instance = instanceSupplier == null ? null : instanceSupplier.get();
        assertEquals(isGESpring5 ? user : null, instance);
    }

    @Test
    public void testSetInstanceSupplierOnNull() {
        AbstractBeanDefinition beanDefinition = null;
        assertEquals(isGESpring5, setInstanceSupplier(beanDefinition, () -> null));

        beanDefinition = this.beanDefinition;
        assertFalse(setInstanceSupplier(beanDefinition, null));
    }

    @Test
    public void testGetInstanceSupplierOnNull() {
        AbstractBeanDefinition beanDefinition = null;
        assertNull(getInstanceSupplier(beanDefinition));
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


    private void testInSpringContainer(BiConsumer<ConfigurableApplicationContext, ConfigurableListableBeanFactory> contextConsumer) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        contextConsumer.accept(context, context.getBeanFactory());
        context.close();
    }
}
