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

import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static io.microsphere.spring.beans.factory.support.BeanDefinitionBuilderUtils.genericBeanDefinitionBuilder;
import static io.microsphere.spring.beans.factory.support.BeanDefinitionBuilderUtils.initBeanDefinitionBuilder;
import static org.junit.Assert.assertEquals;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * {@link BeanDefinitionBuilderUtils}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanDefinitionBuilderUtils
 * @since 1.0.0
 */
public class BeanDefinitionBuilderUtilsTest {

    private String beanName = "test-bean";

    private String bean = "test";

    @Test
    public void testGenericBeanDefinitionBuilder() {
        BeanDefinitionBuilder beanDefinitionBuilder = genericBeanDefinitionBuilder(() -> this.bean);
        assertBeanDefinitionBuilder(beanDefinitionBuilder);
    }

    @Test
    public void testInitBeanDefinitionBuilder() {
        BeanDefinitionBuilder beanDefinitionBuilder = genericBeanDefinition();
        initBeanDefinitionBuilder(true, beanDefinitionBuilder, () -> this.bean);
        initBeanDefinitionBuilder(false, beanDefinitionBuilder, () -> this.bean);
        assertBeanDefinitionBuilder(beanDefinitionBuilder);
    }

    void assertBeanDefinitionBuilder(BeanDefinitionBuilder beanDefinitionBuilder) {
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.freezeConfiguration();
        beanFactory.registerBeanDefinition(this.beanName, beanDefinition);
        assertEquals(this.bean, beanFactory.getBean(this.beanName));
    }
}
