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


import io.microsphere.logging.test.junit4.LoggingLevelsRule;
import io.microsphere.spring.test.domain.User;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import static io.microsphere.logging.test.junit4.LoggingLevelsRule.levels;


/**
 * {@link DependencyAnalysisBeanFactoryListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DependencyAnalysisBeanFactoryListener
 * @since 1.0.0
 */
@ContextConfiguration(classes = DependencyAnalysisBeanFactoryListenerTest.Config.class)
public class DependencyAnalysisBeanFactoryListenerTest extends AbstractEventListenerTest<DependencyAnalysisBeanFactoryListener> {

    @ClassRule
    public static final LoggingLevelsRule LOGGING_LEVELS_RULE = levels("TRACE", "INFO", "ERROR");


    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    /**
     * Passing a different (non-matching) beanFactory triggers the
     * DefaultListableBeanFactory cast and completes without error.
     */
    @Test
    public void testOnBeanFactoryConfigurationFrozenWithAnotherFactory() {
        DefaultListableBeanFactory another = new DefaultListableBeanFactory();
        another.registerBeanDefinition("user", new RootBeanDefinition(User.class));
        // Must not throw; exercises the full analysis path on a fresh factory
        beanFactoryListener.onBeanFactoryConfigurationFrozen(another);
    }

    @Import(User.class)
    static class Config {

        public Config(User user) {
        }

        /**
         * Bean with a dependency (Config depends on User), exercising the
         * "depends on other beans" branch in resolveBeanDefinitionDependentBeanNames.
         */
        @Bean
        public String greeting(User user) {
            return "Hello " + user.getName();
        }
    }

}