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

import io.microsphere.spring.test.Bean;
import io.microsphere.spring.test.TestBean;
import io.microsphere.spring.test.domain.User;
import io.microsphere.spring.util.UserFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

import static io.microsphere.spring.core.io.support.SpringFactoriesLoaderUtils.loadFactories;
import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
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
    public void testLoadFactories() {
        testLoadFactoriesFromContext(beanFactory, context);
        testLoadFactoriesFromBeanFactory(beanFactory);
    }

    @Test
    public void testLoadFactoriesWithArguments() {
        List<User> users = loadFactories(context, User.class, EMPTY_OBJECT_ARRAY);
        assertUser(users);
    }

    @Test
    public void testLoadFactoriesWithArgumentsOnConstructorNotFound() {
        assertThrows(IllegalArgumentException.class, () -> loadFactories(context, User.class, 18, "Mercy"));
    }

    @Test
    public void testLoadFactoriesOnNotFound() {
        assertSame(emptyList(), loadFactories(context, UserFactory.class, "Mercy", 18));
    }

    @Test
    public void testLoadFactoriesOnNull() {
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
        assertNull(testBean.getApplicationContext());
        assertNull(testBean.getApplicationEventPublisher());
        assertNull(testBean.getResourceLoader());
        assertNull(testBean.getMessageSource());
        assertNull(testBean.getEnvironment());
        assertNull(testBean.getResolver());

        assertSame(beanFactory, testBean.getBeanFactory());

        assertEquals("io.microsphere.spring.test.TestBean#0", testBean.getBeanName());
    }

    private void testLoadFactoriesFromContext(ConfigurableBeanFactory beanFactory, ApplicationContext context) {
        List<Bean> beans = loadFactories(context, Bean.class);

        assertEquals(2, beans.size());

        TestBean testBean = (TestBean) beans.get(0);

        assertNotNull(testBean);
        assertNotNull(testBean.getResolver());

        assertSame(context, testBean.getApplicationContext());
        assertSame(context, testBean.getApplicationEventPublisher());
        assertSame(context, testBean.getResourceLoader());
        assertSame(context, testBean.getMessageSource());
        assertSame(context.getEnvironment(), testBean.getEnvironment());

        assertSame(beanFactory, testBean.getBeanFactory());
        assertSame(beanFactory.getBeanClassLoader(), testBean.getClassLoader());

        assertEquals("io.microsphere.spring.test.TestBean#0", testBean.getBeanName());
    }
}
