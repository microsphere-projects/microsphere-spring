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

package io.microsphere.spring.context.annotation;

import io.microsphere.spring.config.context.annotation.DefaultPropertiesPropertySource;
import io.microsphere.spring.context.config.Test1AutoRegistrationBean;
import io.microsphere.spring.context.config.Test2AutoRegistrationBean;
import io.microsphere.spring.test.junit.jupiter.SpringLoggingTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link EnableAutoRegistrationBean} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableAutoRegistrationBean
 * @since 1.0.0
 */
@SpringLoggingTest
class EnableAutoRegistrationBeanTest {

    @Test
    void testDefaultConfig() {
        testInSpringContainer(context -> {
            assertTrue(isBeanPresent(context, DefaultConfig.class));
            assertTrue(context.containsBean(Test1AutoRegistrationBean.BEAN_NAME));
            assertTrue(context.containsBean(Test2AutoRegistrationBean.BEAN_NAME));
        }, DefaultConfig.class);
    }

    @Test
    void testDisabledConfig() {
        testInSpringContainer(context -> {
            assertTrue(isBeanPresent(context, DisalbedConfig.class));
            assertFalse(context.containsBean(Test1AutoRegistrationBean.BEAN_NAME));
            assertFalse(context.containsBean(Test2AutoRegistrationBean.BEAN_NAME));
        }, DisalbedConfig.class);
    }

    @Test
    void testDisabledTest1Config() {
        testInSpringContainer(context -> {
            assertTrue(isBeanPresent(context, DisalbedTest1Config.class));
            assertFalse(context.containsBean(Test1AutoRegistrationBean.BEAN_NAME));
            assertTrue(context.containsBean(Test2AutoRegistrationBean.BEAN_NAME));
        }, DisalbedTest1Config.class);
    }

    void testDisabledTest2Config() {
        testInSpringContainer(context -> {
            assertTrue(isBeanPresent(context, DisalbedTest2Config.class));
            assertTrue(context.containsBean(Test1AutoRegistrationBean.BEAN_NAME));
            assertFalse(context.containsBean(Test2AutoRegistrationBean.BEAN_NAME));
        }, DisalbedTest2Config.class);
    }

    @Test
    void testDisabledAllConfig() {
        testInSpringContainer(context -> {
            assertTrue(isBeanPresent(context, DisalbedAllConfig.class));
            assertFalse(context.containsBean(Test1AutoRegistrationBean.BEAN_NAME));
            assertFalse(context.containsBean(Test2AutoRegistrationBean.BEAN_NAME));
        }, DisalbedAllConfig.class);
    }

    @Test
    void testDuplicatedConfig() {
        testInSpringContainer(context -> {
            assertTrue(isBeanPresent(context, DuplicatedConfig.class));
            assertTrue(context.containsBean(Test1AutoRegistrationBean.BEAN_NAME));
            assertTrue(context.containsBean(Test2AutoRegistrationBean.BEAN_NAME));
        }, DuplicatedConfig.class);
    }

    @EnableAutoRegistrationBean
    static class DefaultConfig {
    }

    @DefaultPropertiesPropertySource(properties = {
            "microsphere.spring.beans.auto-registered=false"
    })
    @EnableAutoRegistrationBean
    static class DisalbedConfig {
    }

    @DefaultPropertiesPropertySource(properties = {
            "microsphere.spring.beans.test-1.auto-registered=false"
    })
    @EnableAutoRegistrationBean
    static class DisalbedTest1Config {
    }

    @DefaultPropertiesPropertySource(properties = {
            "microsphere.spring.beans.test-2.auto-registered=false"
    })
    @EnableAutoRegistrationBean
    static class DisalbedTest2Config {
    }

    @DefaultPropertiesPropertySource(properties = {
            "microsphere.spring.beans.test-1.auto-registered=false",
            "microsphere.spring.beans.test-2.auto-registered=false"
    })
    @EnableAutoRegistrationBean
    static class DisalbedAllConfig {
    }

    @EnableAutoRegistrationBean
    @Import(DefaultConfig.class)
    static class DuplicatedConfig {
    }
}