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
package io.microsphere.spring.beans.factory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asAutowireCapableBeanFactory;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableBeanFactory;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableListableBeanFactory;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asDefaultListableBeanFactory;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asHierarchicalBeanFactory;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asListableBeanFactory;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.getBeanPostProcessors;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.getBeans;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.getOptionalBean;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.getResolvableDependencyTypes;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.isBeanDefinitionRegistry;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.isDefaultListableBeanFactory;
import static io.microsphere.spring.context.ApplicationContextUtils.APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS_NAME;
import static io.microsphere.util.ArrayUtils.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link BeanFactoryUtils} Test
 *
 * @since 1.0.0
 */
public class BeanFactoryUtilsTest {

    private AnnotationConfigApplicationContext applicationContext;

    private ConfigurableListableBeanFactory beanFactory;

    @Before
    public void init() {
        this.applicationContext = new AnnotationConfigApplicationContext();
        this.beanFactory = this.applicationContext.getBeanFactory();
    }

    @After
    public void destroy() {
        applicationContext.close();
    }

    @Test
    public void testGetOptionalBean() {

        applicationContext.register(BaseTestBean.class);

        applicationContext.refresh();

        BaseTestBean testBean = getOptionalBean(applicationContext, "baseTestBean", BaseTestBean.class);

        Assert.assertNotNull(testBean);

        assertEquals("Hello,World", testBean.getName());

    }

    @Test
    public void testGetOptionalBeanIfAbsent() {

        applicationContext.refresh();

        BaseTestBean testBean = getOptionalBean(applicationContext, "baseTestBean", BaseTestBean.class);

        Assert.assertNull(testBean);

        testBean = getOptionalBean(applicationContext, "1", BaseTestBean.class);

        Assert.assertNull(testBean);

        testBean = getOptionalBean(applicationContext, null, BaseTestBean.class);

        Assert.assertNull(testBean);
    }

    @Test
    public void testGetBeans() {

        applicationContext.register(BaseTestBean.class, BaseTestBean2.class);

        applicationContext.refresh();

        List<BaseTestBean> testBeans = getBeans(applicationContext, new String[]{"baseTestBean"}, BaseTestBean.class);

        assertEquals(1, testBeans.size());

        assertEquals("Hello,World", testBeans.get(0).getName());

        testBeans = getBeans(applicationContext, (String[]) null, BaseTestBean.class);

        assertEquals(0, testBeans.size());

        testBeans = getBeans(applicationContext, of((String) null), BaseTestBean.class);

        assertEquals(0, testBeans.size());

        testBeans = getBeans(applicationContext, of("abc"), BaseTestBean.class);

        assertEquals(0, testBeans.size());
    }

    @Test
    public void testGetBeansIfAbsent() {

        applicationContext.refresh();

        List<BaseTestBean> testBeans = getBeans(applicationContext, new String[]{"baseTestBean"}, BaseTestBean.class);

        assertTrue(testBeans.isEmpty());

    }

    @Test
    public void testIsMethods() {
        assertTrue(isDefaultListableBeanFactory(this.beanFactory));
        assertTrue(isBeanDefinitionRegistry(this.beanFactory));
    }

    @Test
    public void testAsMethods() {
        assertSame(this.beanFactory, asBeanDefinitionRegistry(this.beanFactory));
        assertSame(this.beanFactory, asListableBeanFactory(this.beanFactory));
        assertSame(this.beanFactory, asHierarchicalBeanFactory(this.beanFactory));
        assertSame(this.beanFactory, asConfigurableBeanFactory(this.beanFactory));
        assertSame(this.beanFactory, asAutowireCapableBeanFactory(this.beanFactory));
        assertSame(this.beanFactory, asConfigurableListableBeanFactory(this.beanFactory));
        assertSame(this.beanFactory, asDefaultListableBeanFactory(this.beanFactory));
    }

    @Test
    public void testGetResolvableDependencyTypes() {
        this.applicationContext.refresh();
        assertEquals(ofSet(BeanFactory.class, ResourceLoader.class, ApplicationEventPublisher.class, ApplicationContext.class),
                getResolvableDependencyTypes(this.beanFactory));
    }

    @Test
    public void testGetBeanPostProcessors() {
        this.applicationContext.refresh();
        List<BeanPostProcessor> beanPostProcessors = getBeanPostProcessors(this.beanFactory);
        assertFalse(beanPostProcessors.isEmpty());
        assertEquals(APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS_NAME, beanPostProcessors.get(0).getClass().getName());
    }


    @Component("baseTestBean2")
    private static class BaseTestBean2 extends BaseTestBean {

    }

    @Component("baseTestBean")
    private static class BaseTestBean {

        private String name = "Hello,World";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
