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

package io.microsphere.spring.test.context.annotation;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertSame;

/**
 * {@link TestConditionContext} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see TestConditionContext
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        TestConditionContext.class
})
public class TestConditionContextTest {

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private TestConditionContext testConditionContext;

    @Test
    public void testGetRegistry() {
        assertSame(this.context.getBeanFactory(), testConditionContext.getRegistry());
    }

    @Test
    public void testGetBeanFactory() {
        assertSame(this.context.getBeanFactory(), testConditionContext.getBeanFactory());
    }

    @Test
    public void testGetEnvironment() {
        assertSame(this.context.getEnvironment(), testConditionContext.getEnvironment());
    }

    @Test
    public void testGetResourceLoader() {
        assertSame(this.context, testConditionContext.getResourceLoader());
    }

    @Test
    public void testGetClassLoader() {
        assertSame(this.context.getClassLoader(), testConditionContext.getClassLoader());
    }
}