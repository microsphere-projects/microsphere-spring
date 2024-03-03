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
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

import static io.microsphere.spring.util.SpringFactoriesLoaderUtils.loadFactories;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * {@link SpringFactoriesLoaderUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see SpringFactoriesLoaderUtils
 * @since 1.0.0
 */
public class SpringFactoriesLoaderUtilsTest {

    private GenericApplicationContext context;

    private DefaultListableBeanFactory beanFactory;

    @Before
    public void before() {
        context = new GenericApplicationContext();
        beanFactory = context.getDefaultListableBeanFactory();
    }

    @Test
    public void testLoadFactories() {
        testLoadFactoriesFromContext(beanFactory, context);
        testLoadFactoriesFromBeanFactory(beanFactory);
    }

    private void testLoadFactoriesFromBeanFactory(BeanFactory beanFactory) {
        List<Bean> beans = loadFactories(beanFactory, Bean.class);

        assertEquals(2, beans.size());

        TestBean testBean = (TestBean) beans.get(0);
        assertNull(testBean.getApplicationContext());
        assertNull(testBean.getApplicationEventPublisher());
        assertNull(testBean.getApplicationStartup());
        assertNull(testBean.getResourceLoader());
        assertNull(testBean.getMessageSource());
        assertNull(testBean.getEnvironment());
        assertNull(testBean.getResolver());

        assertSame(beanFactory, testBean.getBeanFactory());

        assertEquals("io.microsphere.spring.util.TestBean#0", testBean.getBeanName());
    }

    private void testLoadFactoriesFromContext(ConfigurableBeanFactory beanFactory, ApplicationContext context) {
        List<Bean> beans = loadFactories(context, Bean.class);

        assertEquals(2, beans.size());

        TestBean testBean = (TestBean) beans.get(0);

        assertNotNull(testBean);
        assertNotNull(testBean.getResolver());
        assertNotNull(testBean.getApplicationStartup());

        assertSame(context, testBean.getApplicationContext());
        assertSame(context, testBean.getApplicationEventPublisher());
        assertSame(context, testBean.getResourceLoader());
        assertSame(context, testBean.getMessageSource());
        assertSame(context.getEnvironment(), testBean.getEnvironment());

        assertSame(beanFactory, testBean.getBeanFactory());
        assertSame(beanFactory.getBeanClassLoader(), testBean.getClassLoader());

        assertEquals("io.microsphere.spring.util.TestBean#0", testBean.getBeanName());
    }
}
