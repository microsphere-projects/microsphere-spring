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

package io.microsphere.spring.beans.factory.annotation;


import io.microsphere.logging.test.junit4.LoggingLevelsRule;
import io.microsphere.spring.test.domain.User;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static io.microsphere.logging.test.junit4.LoggingLevelsRule.levels;
import static io.microsphere.spring.beans.factory.annotation.EnableConfigurationBeanBinding.DEFAULT_IGNORE_INVALID_FIELDS;
import static io.microsphere.spring.beans.factory.annotation.EnableConfigurationBeanBinding.DEFAULT_IGNORE_UNKNOWN_FIELDS;
import static io.microsphere.spring.beans.factory.annotation.EnableConfigurationBeanBinding.DEFAULT_MULTIPLE;
import static java.lang.String.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * {@link ConfigurationBeanBindingRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationBeanBindingRegistrar
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class ConfigurationBeanBindingRegistrarTest {

    @ClassRule
    public static final LoggingLevelsRule LOGGING_LEVELS_RULE = levels("TRACE", "INFO", "ERROR");


    private AnnotationConfigApplicationContext context;

    private DefaultListableBeanFactory beanFactory;

    private MockEnvironment environment;

    private ConfigurationBeanBindingRegistrar registrar;

    private AnnotationAttributes attributes;

    @Before
    public void setUp() {
        this.context = new AnnotationConfigApplicationContext();
        this.beanFactory = (DefaultListableBeanFactory) this.context.getBeanFactory();
        this.environment = new MockEnvironment();
        this.registrar = new ConfigurationBeanBindingRegistrar();
        this.registrar.setBeanFactory(this.beanFactory);
        this.registrar.setEnvironment(this.environment);
        this.attributes = new AnnotationAttributes();

        this.attributes.put("prefix", "user");
        this.attributes.put("type", User.class);
        this.attributes.put("multiple", DEFAULT_MULTIPLE);
        this.attributes.put("ignoreUnknownFields", DEFAULT_IGNORE_UNKNOWN_FIELDS);
        this.attributes.put("ignoreInvalidFields", DEFAULT_IGNORE_INVALID_FIELDS);
    }

    @After
    public void tearDown() {
        this.context.close();
    }

    @Test
    public void testOnDefaults() {
        assertSingle(null, "Mercy", 18);
    }

    @Test
    public void testOnSingle() {
        assertSingle("u0", "Ma", 28);
    }

    @Test
    public void testOnMultiple() {
        this.attributes.put("prefix", "users");
        this.attributes.put("multiple", true);

        this.environment.setProperty("users.u0", "-");
        this.environment.setProperty("users.u1.name", "Mercy");
        this.environment.setProperty("users.u1.age", "18");

        this.environment.setProperty("users.u2.name", "Ma");
        this.environment.setProperty("users.u2.age", "28");

        this.registrar.registerConfigurationBeanDefinitions(this.attributes, this.beanFactory);

        this.context.refresh();

        assertUserBean("u1", "Mercy", 18);
        assertUserBean("u2", "Ma", 28);
    }

    void assertSingle(String beanName, String name, int age) {
        if (beanName != null) {
            this.environment.setProperty("user.id", beanName);
        }
        this.environment.setProperty("user.name", name);
        this.environment.setProperty("user.age", valueOf(age));

        this.registrar.registerConfigurationBeanDefinitions(this.attributes, this.beanFactory);

        this.context.refresh();

        assertUserBean(beanName, name, age);
    }

    private void assertUserBean(String id, String name, int age) {
        Map<String, User> beansMap = this.context.getBeansOfType(User.class);
        String beanName = id == null ? User.class.getName() + "#0" : id;
        User user = beansMap.get(beanName);
        assertNotNull(user);
        assertEquals(name, user.getName());
        assertEquals(age, user.getAge());
    }
}