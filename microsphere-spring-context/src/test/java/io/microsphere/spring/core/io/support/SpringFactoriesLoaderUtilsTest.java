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
package io.microsphere.spring.core.io.support;

import io.microsphere.spring.beans.test.Bean;
import io.microsphere.spring.beans.test.TestBean;
import io.microsphere.spring.test.domain.User;
import io.microsphere.spring.util.UserFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

import static io.microsphere.spring.core.io.support.SpringFactoriesLoaderUtils.loadFactories;
import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link SpringFactoriesLoaderUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see SpringFactoriesLoaderUtils
 * @since 1.0.0
 */
class SpringFactoriesLoaderUtilsTest {

    private GenericApplicationContext context;

    private DefaultListableBeanFactory beanFactory;

    private static final Object[] ARGS = ofArray("Mercy", 18);

    @BeforeEach
    void setUp() {
        context = new GenericApplicationContext();
        beanFactory = context.getDefaultListableBeanFactory();
        context.refresh();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void testLoadFactories() {
        testLoadFactoriesFromContext(beanFactory, context);
        testLoadFactoriesFromBeanFactory(beanFactory);
        testLoadFactoriesFromBeanFactory(this.context);
    }

    @Test
    void testLoadFactoriesWithArguments() {
        List<User> users = loadFactories(context, User.class, EMPTY_OBJECT_ARRAY);
        assertUser(users);

        users = loadFactories(context, User.class, ARGS);
        assertUser(users, ARGS);

        users = loadFactories(null, User.class, ARGS);
        assertUser(users, ARGS);
    }

    @Test
    void testLoadFactoriesWithArgumentsOnConstructorNotFound() {
        assertThrows(IllegalArgumentException.class, () -> loadFactories(context, User.class, 18, "Mercy"));
    }

    @Test
    void testLoadFactoriesOnNotFound() {
        assertSame(emptyList(), loadFactories(context, UserFactory.class, ARGS));
        assertSame(emptyList(), loadFactories(null, UserFactory.class, ARGS));
    }

    @Test
    void testLoadFactoriesOnNull() {
        assertEquals(emptyList(), loadFactories(null, UserFactory.class));
        assertEquals(emptyList(), loadFactories((BeanFactory) null, UserFactory.class));
    }

    private void assertUser(List<User> users, Object... args) {
        assertEquals(1, users.size());
        User user = users.get(0);
        assertNotNull(user);
        switch (args.length) {
            case 2:
                assertEquals(args[1], user.getAge());
            case 1:
                assertEquals(args[0], user.getName());
            default:
                break;
        }
    }

    private void testLoadFactoriesFromBeanFactory(BeanFactory beanFactory) {
        List<Bean> beans = loadFactories(beanFactory, Bean.class);
        assertEquals(2, beans.size());
        TestBean testBean = (TestBean) beans.get(0);
        assertTestBean(testBean, beanFactory);
    }

    private void testLoadFactoriesFromContext(ConfigurableBeanFactory beanFactory, ConfigurableApplicationContext context) {
        List<Bean> beans = loadFactories(context, Bean.class);
        assertEquals(2, beans.size());
        TestBean testBean = (TestBean) beans.get(0);
        assertTestBean(testBean, context);
    }

    private void assertTestBean(TestBean testBean, BeanFactory beanFactory) {
        assertNotNull(testBean);
        assertEquals("io.microsphere.spring.beans.test.TestBean#0", testBean.getBeanName());

        if (beanFactory instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext context = (ConfigurableApplicationContext) beanFactory;
            assertNotNull(testBean.getResolver());
            assertSame(context.getBeanFactory().getBeanClassLoader(), testBean.getClassLoader());
            assertSame(context.getBeanFactory(), testBean.getBeanFactory());
            assertSame(context, testBean.getApplicationContext());
            assertSame(context, testBean.getApplicationEventPublisher());
            assertSame(context, testBean.getResourceLoader());
            assertSame(context, testBean.getMessageSource());
            assertSame(context.getEnvironment(), testBean.getEnvironment());
        } else {
            assertSame(((ConfigurableBeanFactory) beanFactory).getBeanClassLoader(), testBean.getClassLoader());
            assertSame(beanFactory, testBean.getBeanFactory());
            assertNull(testBean.getApplicationEventPublisher());
            assertNull(testBean.getResourceLoader());
            assertNull(testBean.getMessageSource());
            assertNull(testBean.getEnvironment());
            assertNull(testBean.getResolver());
        }
    }
}
