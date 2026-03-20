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
package io.microsphere.spring.context.event;


import io.microsphere.spring.test.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link ParallelPreInstantiationSingletonsBeanFactoryListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ParallelPreInstantiationSingletonsBeanFactoryListener
 * @since 1.0.0
 */
@ContextConfiguration(classes = ParallelPreInstantiationSingletonsBeanFactoryListenerTest.Config.class)
@TestPropertySource(properties = {
        ParallelPreInstantiationSingletonsBeanFactoryListener.THREADS_PROPERTY_NAME + "=2",
        ParallelPreInstantiationSingletonsBeanFactoryListener.THREAD_NAME_PREFIX_PROPERTY_NAME + "=TestThread-"
})
class ParallelPreInstantiationSingletonsBeanFactoryListenerTest extends AbstractEventListenerTest<ParallelPreInstantiationSingletonsBeanFactoryListener> {

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    /**
     * Listener is registered and injected by the AbstractEventListenerTest infrastructure.
     */
    @Test
    @Override
    void test() {
        assertNotNull(beanFactoryListener);
    }

    /**
     * Passing a different (non-matching) beanFactory logs a warning and returns immediately
     * without throwing.
     */
    @Test
    void testOnBeanFactoryConfigurationFrozenWithWrongFactory() {
        DefaultListableBeanFactory anotherFactory = new DefaultListableBeanFactory();
        anotherFactory.registerBeanDefinition("user", new RootBeanDefinition(User.class));
        // Must not throw
        beanFactoryListener.onBeanFactoryConfigurationFrozen(anotherFactory);
    }

    /**
     * threads=0 disables parallel pre-instantiation (executor is not created).
     */
    @Test
    void testOnBeanFactoryConfigurationFrozenThreadsZero() {
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
        ParallelPreInstantiationSingletonsBeanFactoryListener listener =
                new ParallelPreInstantiationSingletonsBeanFactoryListener();
        listener.setBeanFactory(factory);

        // Use a mock environment that returns 0 for the threads property
        org.springframework.mock.env.MockEnvironment env = new org.springframework.mock.env.MockEnvironment();
        env.setProperty(ParallelPreInstantiationSingletonsBeanFactoryListener.THREADS_PROPERTY_NAME, "0");
        listener.setEnvironment(env);

        // Must not throw and must complete quickly since no threads are created
        listener.onBeanFactoryConfigurationFrozen(factory);
    }

    @Import(User.class)
    static class Config {
        public Config(User user) {}
    }
}