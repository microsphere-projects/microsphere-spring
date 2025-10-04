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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.Assert.assertTrue;

/**
 * {@link DefaultBeanDependencyResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultBeanDependencyResolver
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = DefaultBeanDependencyResolverTest.class)
public class DefaultBeanDependencyResolverTest {

    private DefaultBeanDependencyResolver resolver;

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    private ExecutorService executorService;

    @Before
    public void setUp() {
        this.executorService = newSingleThreadExecutor();
        this.resolver = new DefaultBeanDependencyResolver(this.beanFactory, this.executorService);
    }

    @After
    public void tearDown() {
        this.executorService.shutdown();
    }

    @Test
    public void testResolve() {
        Map<String, Set<String>> dependentBeanNamesMap = this.resolver.resolve(this.beanFactory);
        assertTrue(dependentBeanNamesMap.isEmpty());
    }
}