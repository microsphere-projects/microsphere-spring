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
package io.microsphere.spring.core.env;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.Profiles;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ListenableConfigurableEnvironment} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = ListenableConfigurableEnvironmentTest.class,
        initializers = ListenableConfigurableEnvironmentInitializer.class
)
@TestPropertySource(
        properties = {
                "microsphere.spring.listenable-environment.enabled=true",
                "spring.profiles.active=test",
                "user.name=Mercy",
                "score=99"
        }
)
class ListenableConfigurableEnvironmentTest {

    @Autowired
    private ListenableConfigurableEnvironment environment;

    /**
     * Test {@link ListenableConfigurableEnvironment#setActiveProfiles(String...)}
     */
    @Test
    void testSetActiveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        environment.setActiveProfiles(activeProfiles);
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#addActiveProfile(String)}
     */
    @Test
    void testAddActiveProfile() {
        environment.addActiveProfile("dev");
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#setDefaultProfiles(String...)}
     */
    @Test
    void testSetDefaultProfiles() {
        environment.setDefaultProfiles("default");
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#getPropertySources()}
     */
    @Test
    void testGetPropertySources() {
        MutablePropertySources propertySources = environment.getPropertySources();
        assertNotNull(propertySources);
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#getSystemEnvironment()}
     */
    @Test
    void testGetSystemProperties() {
        assertSame(System.getProperties(), environment.getSystemProperties());
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#getSystemEnvironment()}
     */
    @Test
    void testGetSystemEnvironment() {
        assertSame(System.getenv(), environment.getSystemEnvironment());
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#merge(ConfigurableEnvironment)}
     */
    @Test
    void testMerge() {
        environment.merge(environment);
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#getActiveProfiles()}
     */
    @Test
    void testGetActiveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        assertEquals(1, activeProfiles.length);
        assertEquals("test", activeProfiles[0]);
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#getDefaultProfiles()}
     */
    @Test
    void testGetDefaultProfiles() {
        String[] defaultProfiles = environment.getDefaultProfiles();
        assertEquals(1, defaultProfiles.length);
        assertEquals("default", defaultProfiles[0]);
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#matchesProfiles(String...)}
     */
    @Test
    void testMatchesProfiles() {
        assertTrue(environment.matchesProfiles("test"));
        assertFalse(environment.matchesProfiles("!test"));

    }

    /**
     * Test
     * <ul>
     *     <li>{@link ListenableConfigurableEnvironment#acceptsProfiles(String...)}</li>
     *     <li>{@link ListenableConfigurableEnvironment#acceptsProfiles(Profiles)}</li>
     * </ul>
     */
    @Test
    void testAcceptsProfiles() {
        assertTrue(environment.acceptsProfiles("test"));
        assertTrue(environment.acceptsProfiles(profile -> true));
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#containsProperty(String)}
     */
    @Test
    void testContainsProperty() {
        assertTrue(environment.containsProperty("user.name"));
    }

    /**
     * Test
     * <ul>
     *     <li>{@link ListenableConfigurableEnvironment#getProperty(String)}</li>
     *     <li>{@link ListenableConfigurableEnvironment#getProperty(String, String)}</li>
     *     <li>{@link ListenableConfigurableEnvironment#getProperty(String, Class)}</li>
     *     <li>{@link ListenableConfigurableEnvironment#getProperty(String, Class, Object)}</li>
     * </ul>
     */
    @Test
    void testGetProperty() {
        String userName = environment.getProperty("user.name");
        assertEquals("Mercy", userName);

        String notFound = environment.getProperty("not.found", "not.found");
        assertEquals("not.found", notFound);

        int scope = environment.getProperty("score", int.class);
        assertEquals(99, scope);

        scope = environment.getProperty("your.score", int.class, 100);
        assertEquals(100, scope);
    }

    /**
     * Test
     * <ul>
     *     <li>{@link ListenableConfigurableEnvironment#getRequiredProperty(String)}</li>
     *     <li>{@link ListenableConfigurableEnvironment#getRequiredProperty(String, Class)}</li>
     * </ul>
     */
    @Test
    void testGetRequiredProperty() {
        String userName = environment.getRequiredProperty("user.name");
        assertEquals("Mercy", userName);

        int scope = environment.getRequiredProperty("score", int.class);
        assertEquals(99, scope);
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#resolvePlaceholders(String)}
     */
    @Test
    void testResolvePlaceholders() {
        String userName = environment.resolvePlaceholders("${user.name}");
        assertEquals("Mercy", userName);
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#resolveRequiredPlaceholders(String)}
     */
    @Test
    void testResolveRequiredPlaceholders() {
        String userName = environment.resolveRequiredPlaceholders("${user.name}");
        assertEquals("Mercy", userName);
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#getConversionService()}
     */
    @Test
    void testGetConversionService() {
        ConfigurableConversionService conversionService = environment.getConversionService();
        assertNotNull(conversionService);
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#setConversionService(ConfigurableConversionService)}
     */
    @Test
    void testSetConversionService() {
        environment.setConversionService(new DefaultFormattingConversionService());
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#setPlaceholderSuffix(String)}
     */
    @Test
    void testSetPlaceholderPrefix() {
        environment.setPlaceholderPrefix("${");
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#setPlaceholderSuffix(String)}
     */
    @Test
    void testSetPlaceholderSuffix() {
        environment.setPlaceholderSuffix("}");
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#setValueSeparator(String)}
     */
    @Test
    void testSetValueSeparator() {
        environment.setValueSeparator("#");
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#setIgnoreUnresolvableNestedPlaceholders(boolean)}
     */
    @Test
    void testSetIgnoreUnresolvableNestedPlaceholders() {
        environment.setIgnoreUnresolvableNestedPlaceholders(true);
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#setRequiredProperties(String...)}
     */
    @Test
    void testSetRequiredProperties() {
        environment.setRequiredProperties("user.name");
    }

    /**
     * Test {@link ListenableConfigurableEnvironment#validateRequiredProperties()}
     */
    @Test
    void testValidateRequiredProperties() {
        environment.validateRequiredProperties();
    }

    @Test
    void testGetDelegate() {
        assertInstanceOf(ListenableConfigurableEnvironment.class, environment);
        assertNotSame(environment, environment.getDelegate());
    }
}
