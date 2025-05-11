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

package io.microsphere.spring.config.context.annotation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME;

/**
 * {@link ResourcePropertySourceLoader} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySourceLoader
 * @see ResourcePropertySource
 * @since 1.0.0
 */
public class ResourcePropertySourceLoaderTest {

    static final Class<ResourcePropertySource> RESOURCE_PROPERTY_SOURCE_CLASS = ResourcePropertySource.class;

    @Before
    public void before() {

    }

    @After
    public void after() {

    }

    @Test
    public void testOnDefaultConfig() {
        testInSpringContainer((context, environment) -> {
            assertDefaultPropertySource(environment, DefaultConfig.class);
        }, DefaultConfig.class);
    }

    @Test
    public void testOnNamedConfig() {
        testInSpringContainer((context, environment) -> {
            assertNamedPropertySource(environment, "test-property-source");
        }, NamedConfig.class);
    }

    @Test
    public void testOnFirstConfig() {
        testInSpringContainer((context, environment) -> {
            assertFirstPropertySource(environment, FirstConfig.class);
        }, FirstConfig.class);
    }

    @Test
    public void testOnBeforeConfig() {
        testInSpringContainer((context, environment) -> {
            assertBeforePropertySource(environment, BeforeConfig.class);
        }, BeforeConfig.class);
    }

    @Test
    public void testOnAfterConfig() {
        testInSpringContainer((context, environment) -> {
            assertAfterPropertySource(environment, AfterConfig.class);
        }, AfterConfig.class);
    }

    @Test
    public void testOnIgnoreResourceNotFoundConfig() {
        testInSpringContainer((context, environment) -> {
            MutablePropertySources propertySources = environment.getPropertySources();
            assertEquals(2, propertySources.size());
            assertNotNull(propertySources.get(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME));
            assertNotNull(propertySources.get(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME));
        }, IgnoreResourceNotFoundConfig.class);
    }

    @Test(expected = BeanDefinitionStoreException.class)
    public void testOnNotFoundConfig() {
        testInSpringContainer((context, environment) -> {
        }, NotFoundConfig.class);
    }

    @Test
    public void testOnAutoRefreshedConfig() {
        testInSpringContainer((context, environment) -> {
            assertDefaultPropertySource(environment, AutoRefreshedConfig.class);
        }, AutoRefreshedConfig.class);
    }

    void assertDefaultPropertySource(ConfigurableEnvironment environment, Class<?> introspectedClass) {
        PropertySource<?> propertySource = assertLastPropertySource(environment);
        assertDefaultPropertySourceName(propertySource, introspectedClass);
        assertPropertySource(propertySource);
        assertProperties(environment);
    }

    void assertNamedPropertySource(ConfigurableEnvironment environment, String propertySourceName) {
        MutablePropertySources propertySources = environment.getPropertySources();
        PropertySource propertySource = propertySources.get(propertySourceName);
        assertSame(assertLastPropertySource(environment), propertySource);
        assertPropertySource(propertySource);
        assertProperties(environment);
    }

    void assertFirstPropertySource(ConfigurableEnvironment environment, Class<?> introspectedClass) {
        MutablePropertySources propertySources = environment.getPropertySources();
        Iterator<PropertySource<?>> iterator = propertySources.iterator();
        PropertySource<?> propertySource = iterator.next();
        assertDefaultPropertySourceName(propertySource, introspectedClass);
        assertPropertySource(propertySource);
        assertProperties(environment);
    }

    void assertBeforePropertySource(ConfigurableEnvironment environment, Class<?> introspectedClass) {
        MutablePropertySources propertySources = environment.getPropertySources();
        assertEquals(3, propertySources.size());

        Iterator<PropertySource<?>> iterator = propertySources.iterator();
        assertTrue(iterator instanceof ListIterator);
        ListIterator<PropertySource<?>> listIterator = (ListIterator<PropertySource<?>>) iterator;
        ResourcePropertySource resourcePropertySource = introspectedClass.getAnnotation(RESOURCE_PROPERTY_SOURCE_CLASS);
        String before = resourcePropertySource.before();
        while (listIterator.hasNext()) {
            PropertySource<?> propertySource = listIterator.next();
            if (propertySource.getName().equals(before)) {
                break;
            }
        }
        listIterator.previous();
        PropertySource<?> propertySource = listIterator.previous();
        assertDefaultPropertySourceName(propertySource, introspectedClass);
        assertPropertySource(propertySource);
        assertProperties(environment);
    }

    void assertAfterPropertySource(ConfigurableEnvironment environment, Class<?> introspectedClass) {
        MutablePropertySources propertySources = environment.getPropertySources();
        assertEquals(3, propertySources.size());

        Iterator<PropertySource<?>> iterator = propertySources.iterator();
        ResourcePropertySource resourcePropertySource = introspectedClass.getAnnotation(RESOURCE_PROPERTY_SOURCE_CLASS);
        String after = resourcePropertySource.after();
        while (iterator.hasNext()) {
            PropertySource<?> propertySource = iterator.next();
            if (propertySource.getName().equals(after)) {
                break;
            }
        }
        PropertySource<?> propertySource = iterator.next();
        assertDefaultPropertySourceName(propertySource, introspectedClass);
        assertPropertySource(propertySource);
        assertProperties(environment);
    }

    PropertySource<?> assertLastPropertySource(ConfigurableEnvironment environment) {
        MutablePropertySources propertySources = environment.getPropertySources();
        Iterator<PropertySource<?>> iterator = propertySources.iterator();
        int size = propertySources.size();
        PropertySource<?> propertySource = null;
        for (int i = 0; i < size; i++) {
            propertySource = iterator.next();
        }
        return propertySource;
    }

    void assertDefaultPropertySourceName(PropertySource<?> propertySource, Class<?> introspectedClass) {
        String propertySourceName = introspectedClass.getName() + "@" + RESOURCE_PROPERTY_SOURCE_CLASS.getName();
        assertEquals(propertySourceName, propertySource.getName());
    }

    void assertPropertySource(PropertySource<?> propertySource) {
        assertTrue(propertySource instanceof CompositePropertySource);
        CompositePropertySource compositePropertySource = (CompositePropertySource) propertySource;
        Collection<PropertySource<?>> internalPropertySources = compositePropertySource.getPropertySources();
        assertEquals(2, internalPropertySources.size());
        assertProperties(propertySource);
    }

    void assertProperties(PropertySource<?> propertySource) {
        assertEquals("1", propertySource.getProperty("a"));
        assertEquals("3", propertySource.getProperty("b"));
    }

    void assertProperties(ConfigurableEnvironment environment) {
        assertEquals("1", environment.getProperty("a"));
        assertEquals("3", environment.getProperty("b"));
    }

    void testInSpringContainer(BiConsumer<ConfigurableApplicationContext, ConfigurableEnvironment> consumer, Class<?>... configClasses) {
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(configClasses);
        consumer.accept(context, context.getEnvironment());
        context.close();
    }

    @ResourcePropertySource("classpath*:/META-INF/test/*.properties")
    static class DefaultConfig {
    }

    @ResourcePropertySource(
            name = "test-property-source",
            value = "classpath*:/META-INF/test/*.properties"
    )
    static class NamedConfig {
    }

    @ResourcePropertySource(
            value = "classpath*:/META-INF/test/*.properties",
            first = true
    )
    static class FirstConfig {
    }

    @ResourcePropertySource(
            value = "classpath*:/META-INF/test/*.properties",
            before = SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME
    )
    static class BeforeConfig {
    }

    @ResourcePropertySource(
            value = "classpath*:/META-INF/test/*.properties",
            after = SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME
    )
    static class AfterConfig {
    }

    @ResourcePropertySource(
            value = "classpath*:/not-found.properties",
            ignoreResourceNotFound = true
    )
    static class IgnoreResourceNotFoundConfig {
    }

    @ResourcePropertySource("classpath*:/not-found.properties")
    static class NotFoundConfig {
    }

    @ResourcePropertySource(
            value = "classpath*:/META-INF/test/*.properties",
            autoRefreshed = true
    )
    static class AutoRefreshedConfig {
    }
}
