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

package io.microsphere.spring.context;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ConfigurableApplicationContextInitializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurableApplicationContextInitializer
 * @since 1.0.0
 */
class ConfigurableApplicationContextInitializerTest extends ConfigurableApplicationContextInitializer {

    GenericApplicationContext testContext = new GenericApplicationContext();

    MockEnvironment testEnvironment = new MockEnvironment();

    private ConfigurableApplicationContext context;

    private ConfigurableEnvironment environment;

    @BeforeEach
    void setUp() {
        this.testContext.setEnvironment(this.testEnvironment);
    }

    @Test
    void testInitialize() {
        this.initialize(this.testContext);
        assertSame(this.testContext, this.context);
        assertSame(this.testEnvironment, this.environment);

        // reinitialize
        this.initialize(this.testContext);

        this.testEnvironment.setProperty(getEnabledPropertyName(), "false");
        this.initialize(this.testContext);
    }

    @Test
    void testIsEnabled() {

        assertTrue(isEnabled(this.testContext, this.testEnvironment));

        this.testEnvironment.setProperty(getEnabledPropertyName(), "false");
        assertFalse(isEnabled(this.testContext, this.testEnvironment));
    }

    @Test
    void testGetEnabledPropertyName() {
        assertEquals("microsphere.spring.context-initializer.configurableApplicationContextInitializerTest.enabled",
                getEnabledPropertyName());
    }

    @Override
    protected void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        this.context = context;
        this.environment = environment;
    }
}