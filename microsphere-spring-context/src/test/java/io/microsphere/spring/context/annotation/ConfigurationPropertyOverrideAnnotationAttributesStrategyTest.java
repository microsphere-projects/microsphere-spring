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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static io.microsphere.spring.context.annotation.ConfigurationPropertyOverrideAnnotationAttributesStrategy.getDefaultPropertyNamePrefix;
import static io.microsphere.spring.context.annotation.ConfigurationPropertyOverrideAnnotationAttributesStrategy.getPrefixPropertyName;
import static io.microsphere.spring.context.annotation.ConfigurationPropertyOverrideAnnotationAttributesStrategyTest.CLASS_NAME_1;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link ConfigurationPropertyOverrideAnnotationAttributesStrategy} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertyOverrideAnnotationAttributesStrategy
 * @since 1.0.0
 */
@ImportOptional(value = CLASS_NAME_1)
class ConfigurationPropertyOverrideAnnotationAttributesStrategyTest {

    static final String CLASS_NAME_1 = "io.microsphere.spring.context.annotation.ConfigurationPropertyOverrideAnnotationAttributesStrategy";

    static final String CLASS_NAME_2 = "io.microsphere.spring.context.annotation.ConfigurationPropertyOverrideAnnotationAttributesStrategyTest";

    private static final Class<ImportOptional> IMPORT_OPTIONAL_CLASS = ImportOptional.class;

    private static final AnnotationAttributes IMPORT_OPTIONAL_ATTRIBUTTES = getAnnotationAttributes(ConfigurationPropertyOverrideAnnotationAttributesStrategyTest.class,
            IMPORT_OPTIONAL_CLASS, null, false);

    private static final String VALUE_ATTRIBUTE_NAME = "value";


    private MockEnvironment environment;

    private ConfigurationPropertyOverrideAnnotationAttributesStrategy strategy;

    @BeforeEach
    void setUp() {
        this.environment = new MockEnvironment();
        this.strategy = new ConfigurationPropertyOverrideAnnotationAttributesStrategy();
        this.strategy.setEnvironment(environment);

        assertArrayEquals(ofArray(CLASS_NAME_1), IMPORT_OPTIONAL_ATTRIBUTTES.getStringArray(VALUE_ATTRIBUTE_NAME));
    }

    @Test
    void testOverride() {
        String propertyNamePrefix = this.strategy.getPropertyNamePrefix(IMPORT_OPTIONAL_CLASS);
        environment.setProperty(propertyNamePrefix + VALUE_ATTRIBUTE_NAME, CLASS_NAME_1 + "," + CLASS_NAME_2);

        AnnotationAttributes overriddenAttributtes = this.strategy.override(IMPORT_OPTIONAL_CLASS, IMPORT_OPTIONAL_ATTRIBUTTES, null);
        assertArrayEquals(ofArray(CLASS_NAME_1, CLASS_NAME_2), overriddenAttributtes.getStringArray(VALUE_ATTRIBUTE_NAME));
    }

    @Test
    void testOverrideOnSameConfigurationProperties() {
        String propertyNamePrefix = this.strategy.getPropertyNamePrefix(IMPORT_OPTIONAL_CLASS);
        environment.setProperty(propertyNamePrefix + VALUE_ATTRIBUTE_NAME, CLASS_NAME_1);

        AnnotationAttributes overriddenAttributtes = this.strategy.override(IMPORT_OPTIONAL_CLASS, IMPORT_OPTIONAL_ATTRIBUTTES, null);
        assertArrayEquals(IMPORT_OPTIONAL_ATTRIBUTTES.getStringArray(VALUE_ATTRIBUTE_NAME), overriddenAttributtes.getStringArray(VALUE_ATTRIBUTE_NAME));
    }

    @Test
    void testOverrideOnDifferentConfigurationProperties() {
        String propertyNamePrefix = this.strategy.getPropertyNamePrefix(IMPORT_OPTIONAL_CLASS);
        environment.setProperty(propertyNamePrefix + "a", CLASS_NAME_1);

        AnnotationAttributes overriddenAttributtes = this.strategy.override(IMPORT_OPTIONAL_CLASS, IMPORT_OPTIONAL_ATTRIBUTTES, null);
        assertArrayEquals(IMPORT_OPTIONAL_ATTRIBUTTES.getStringArray(VALUE_ATTRIBUTE_NAME), overriddenAttributtes.getStringArray(VALUE_ATTRIBUTE_NAME));
    }

    @Test
    void testOverrieOnNoConfigurationProperties() {
        AnnotationAttributes overriddenAttributtes = this.strategy.override(IMPORT_OPTIONAL_CLASS, IMPORT_OPTIONAL_ATTRIBUTTES, null);
        assertSame(IMPORT_OPTIONAL_ATTRIBUTTES, overriddenAttributtes);
    }

    @Test
    void testGetConfigurationProperties() {
        String propertyNamePrefix = this.strategy.getPropertyNamePrefix(IMPORT_OPTIONAL_CLASS);

        environment.setProperty(propertyNamePrefix + "a", "1");
        environment.setProperty(propertyNamePrefix + "b", "2");
        environment.setProperty(propertyNamePrefix + "c", "3");

        Map<String, Object> configurationProperties = this.strategy.getConfigurationProperties(IMPORT_OPTIONAL_CLASS);
        assertEquals(3, configurationProperties.size());
        assertEquals("1", configurationProperties.get("a"));
        assertEquals("2", configurationProperties.get("b"));
        assertEquals("3", configurationProperties.get("c"));
        assertNull(configurationProperties.get("annotationType"));
    }

    @Test
    void testGetPropertyNamePrefix() {
        String prefixPropertyName = getPrefixPropertyName(IMPORT_OPTIONAL_CLASS);
        this.environment.setProperty(prefixPropertyName, "ms");
        assertEquals("ms.", this.strategy.getPropertyNamePrefix(IMPORT_OPTIONAL_CLASS));
    }

    @Test
    void testGetPrefixPropertyName() {
        String prefixPropertyName = getPrefixPropertyName(IMPORT_OPTIONAL_CLASS);
        assertEquals("microsphere.spring.prefix." + IMPORT_OPTIONAL_CLASS.getName(), prefixPropertyName);
    }

    @Test
    void testGetDefaultPropertyNamePrefix() {
        String defaultPropertyNamePrefix = getDefaultPropertyNamePrefix(IMPORT_OPTIONAL_CLASS);
        assertEquals("microsphere.spring.prefix.ImportOptional.", defaultPropertyNamePrefix);
    }
}