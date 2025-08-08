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

import io.microsphere.lang.function.ThrowableAction;
import io.microsphere.spring.config.env.event.PropertySourcesChangedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.microsphere.io.FileUtils.forceDelete;
import static io.microsphere.lang.function.ThrowableAction.execute;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static java.lang.Thread.sleep;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME;
import static org.springframework.core.io.support.PropertiesLoaderUtils.loadProperties;

/**
 * {@link ResourcePropertySourceLoader} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySourceLoader
 * @see ResourcePropertySource
 * @since 1.0.0
 */
class ResourcePropertySourceLoaderTest {

    static final Class<ResourcePropertySource> RESOURCE_PROPERTY_SOURCE_CLASS = ResourcePropertySource.class;

    static final String PROPERTIES_DIRECTORY_RESOURCE_LOCATION = "classpath:/META-INF/test/";

    static final String PROPERTIES_RESOURCE_LOCATION = PROPERTIES_DIRECTORY_RESOURCE_LOCATION + "*.properties";

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void testOnDefaultConfig() {
        testInSpringContainer((context, environment) -> {
            assertDefaultPropertySource(environment, DefaultConfig.class);
        }, DefaultConfig.class);
    }

    @Test
    void testOnNamedConfig() {
        testInSpringContainer((context, environment) -> {
            assertNamedPropertySource(environment, "test-property-source");
        }, NamedConfig.class);
    }

    @Test
    void testOnFirstConfig() {
        testInSpringContainer((context, environment) -> {
            assertFirstPropertySource(environment, FirstConfig.class);
        }, FirstConfig.class);
    }

    @Test
    void testOnBeforeConfig() {
        testInSpringContainer((context, environment) -> {
            assertBeforePropertySource(environment, BeforeConfig.class);
        }, BeforeConfig.class);
    }

    @Test
    void testOnAfterConfig() {
        testInSpringContainer((context, environment) -> {
            assertAfterPropertySource(environment, AfterConfig.class);
        }, AfterConfig.class);
    }

    @Test
    void testOnIgnoreResourceNotFoundConfig() {
        testInSpringContainer((context, environment) -> {
            MutablePropertySources propertySources = environment.getPropertySources();
            assertEquals(2, propertySources.size());
            assertNotNull(propertySources.get(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME));
            assertNotNull(propertySources.get(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME));
        }, IgnoreResourceNotFoundConfig.class);
    }

    @Test
    void testOnNotFoundConfig() {
        assertThrows(BeanDefinitionStoreException.class,
                () -> testInSpringContainer((context, environment) -> {
                }, NotFoundConfig.class));
    }

    @Test
    void testOnAutoRefreshedConfig() {
        testInSpringContainer((context, environment) -> {
            assertDefaultPropertySource(environment, AutoRefreshedConfig.class);
        }, AutoRefreshedConfig.class);
    }

    @Test
    void testOnFileCreated() {
        testInSpringContainer((context, environment) -> {
            execute(() -> {
                Resource propertiesDirectoryResource = context.getResource(PROPERTIES_DIRECTORY_RESOURCE_LOCATION);
                assertTrue(propertiesDirectoryResource.exists());
                File propertiesDirectory = propertiesDirectoryResource.getFile();
                File cPropertiesFile = new File(propertiesDirectory, "c.properties");
                try {
                    testOnFileCreated(context, cPropertiesFile);
                } finally {
                    // recovery
                    delete(cPropertiesFile);
                }
            });
        }, AutoRefreshedConfig.class);
    }

    @Test
    void testOnFileModified() {
        testInSpringContainer((context, environment) -> {
            execute(() -> {
                Resource bPropertiesResource = context.getResource(PROPERTIES_DIRECTORY_RESOURCE_LOCATION + "b.properties");
                assertTrue(bPropertiesResource.exists());
                Properties bProperties = loadProperties(bPropertiesResource);
                File bPropertiesResourceFile = bPropertiesResource.getFile();
                try {
                    testOnFile(context, bPropertiesResourceFile, new Properties(bProperties));
                } finally {
                    // recovery
                    writePropertiesFile(bPropertiesResourceFile, bProperties);
                }
            });
        }, AutoRefreshedConfig.class);
    }

    @Test
    void testOnFileDeleted() {
        testInSpringContainer((context, environment) -> {
            execute(() -> {
                Resource aPropertiesResource = context.getResource(PROPERTIES_DIRECTORY_RESOURCE_LOCATION + "a.properties");
                assertTrue(aPropertiesResource.exists());
                Properties aProperties = loadProperties(aPropertiesResource);
                File aPropertiesResourceFile = aPropertiesResource.getFile();
                try {
                    testOnFileDeleted(context, aPropertiesResourceFile, removedProperties -> {
                        assertEquals("1", removedProperties.get("a"));
                        assertEquals("3", removedProperties.get("b"));
                    });
                } finally {
                    // recovery
                    writePropertiesFile(aPropertiesResourceFile, aProperties);
                }
            });
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
        assertInstanceOf(ListIterator.class, iterator);
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
        assertInstanceOf(CompositePropertySource.class, propertySource);
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

    void testOnFileCreated(ConfigurableApplicationContext context, File newPropertiesFile) throws Throwable {
        testOnFile(context, newPropertiesFile, new Properties());
    }

    void testOnFile(ConfigurableApplicationContext context, File propertiesFile, Properties properties) throws Throwable {

        // watches the properties file
        AtomicBoolean notified = new AtomicBoolean();

        String propertyName = propertiesFile.getName();
        String propertyValue = randomUUID().toString();

        context.addApplicationListener((ApplicationListener<PropertySourcesChangedEvent>) event -> {
            notified.set(true);
            ConfigurableEnvironment environment = context.getEnvironment();
            assertEquals(propertyValue, environment.getProperty(propertyName));
        });

        // appends the new content
        properties.setProperty(propertyName, propertyValue);

        // waits for being notified
        waits(notified, () -> writePropertiesFile(propertiesFile, properties));
    }

    void testOnFileDeleted(ConfigurableApplicationContext context, File deletedPropertiesFile, Consumer<Map<String, Object>> removedPropertiesConsumer) throws Throwable {
        // watches the properties file
        AtomicBoolean notified = new AtomicBoolean();

        context.addApplicationListener((ApplicationListener<PropertySourcesChangedEvent>) event -> {
            notified.set(true);
            Map<String, Object> removedProperties = event.getRemovedProperties();
            removedPropertiesConsumer.accept(removedProperties);
        });

        // waits for being notified
        waits(notified, () -> delete(deletedPropertiesFile));
    }

    void writePropertiesFile(File propertiesFile, Properties properties) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(propertiesFile)) {
            properties.store(outputStream, null);
        }
    }

    void waits(AtomicBoolean notified, ThrowableAction action) throws Throwable {
        // waits for being notified
        for (int i = 0; i < 100; i++) {
            if (notified.get()) {
                break;
            }
            action.execute();
            sleep(500);
        }
    }

    static void delete(File file) throws IOException {
        if (file.exists()) {
            forceDelete(file);
        }
    }

    @ResourcePropertySource(PROPERTIES_RESOURCE_LOCATION)
    static class DefaultConfig {
    }

    @ResourcePropertySource(
            name = "test-property-source",
            value = PROPERTIES_RESOURCE_LOCATION
    )
    static class NamedConfig {
    }

    @ResourcePropertySource(
            value = PROPERTIES_RESOURCE_LOCATION,
            first = true
    )
    static class FirstConfig {
    }

    @ResourcePropertySource(
            value = PROPERTIES_RESOURCE_LOCATION,
            before = SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME
    )
    static class BeforeConfig {
    }

    @ResourcePropertySource(
            value = PROPERTIES_RESOURCE_LOCATION,
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
            value = PROPERTIES_RESOURCE_LOCATION,
            autoRefreshed = true
    )
    static class AutoRefreshedConfig {
    }
}
