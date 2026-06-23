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


import io.microsphere.spring.context.config.Test1AutoRegistrationBean;
import io.microsphere.spring.context.config.Test2AutoRegistrationBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static io.microsphere.spring.constants.PropertyConstants.DEFAULT_AUTO_REGISTERED_VALUE;
import static io.microsphere.spring.context.annotation.EnableAutoRegistrationBean.BEANS_AUTO_REGISTERED_PROEPRTY_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link AutoRegistrationBeanInitializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AutoRegistrationBeanInitializer
 * @since 1.0.0
 */
@SpringJUnitConfig(
        classes = AutoRegistrationBeanInitializerTest.class,
        initializers = AutoRegistrationBeanInitializer.class
)
class AutoRegistrationBeanInitializerTest {

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private AutoRegistrationBeanInitializer autoRegistrationBeanInitializer;

    @Test
    void testInitialize() {
        assertDoesNotThrow(() -> autoRegistrationBeanInitializer.initialize(this.context));
        assertNotNull(this.context.getBean(Test1AutoRegistrationBean.class));
        assertNotNull(this.context.getBean(Test2AutoRegistrationBean.class));
    }

    @Test
    void testGetEnabledPropertyName() {
        assertEquals(BEANS_AUTO_REGISTERED_PROEPRTY_NAME, this.autoRegistrationBeanInitializer.getEnabledPropertyName());
    }

    @Test
    void testGetDefaultEnabled() {
        assertEquals(DEFAULT_AUTO_REGISTERED_VALUE, this.autoRegistrationBeanInitializer.getDefaultEnabled());
    }
}