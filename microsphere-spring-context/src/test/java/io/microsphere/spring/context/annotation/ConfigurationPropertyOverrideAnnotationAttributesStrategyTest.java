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


import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static io.microsphere.spring.context.annotation.ConfigurationPropertyOverrideAnnotationAttributesStrategy.getDefaultPropertyNamePrefix;
import static io.microsphere.spring.context.annotation.ConfigurationPropertyOverrideAnnotationAttributesStrategy.getPrefixPropertyName;
import static io.microsphere.spring.context.annotation.ConfigurationPropertyOverrideAnnotationAttributesStrategyTest.VALUE_1;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * {@link ConfigurationPropertyOverrideAnnotationAttributesStrategy} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertyOverrideAnnotationAttributesStrategy
 * @since 1.0.0
 */
@PropertySource(value = VALUE_1)
public class ConfigurationPropertyOverrideAnnotationAttributesStrategyTest {

    static final String VALUE_1 = "classpath:/com/myco/app.properties";

    static final String VALUE_2 = "file:/path/to/file.xml";

    private static final Class<PropertySource> ANNOTATION_CLASS = PropertySource.class;

    private static final AnnotationAttributes ATTRIBUTES = getAnnotationAttributes(ConfigurationPropertyOverrideAnnotationAttributesStrategyTest.class,
            ANNOTATION_CLASS, null, false);

    private static final String VALUE_ATTRIBUTE_NAME = "value";

    private static final String FACTORY_ATTRIBUTE_NAME = "factory";


    private MockEnvironment environment;

    private ConfigurationPropertyOverrideAnnotationAttributesStrategy strategy;

    @Before
    public void setUp() {
        this.environment = new MockEnvironment();
        this.strategy = new ConfigurationPropertyOverrideAnnotationAttributesStrategy();
        this.strategy.setEnvironment(environment);

        assertArrayEquals(ofArray(VALUE_1), ATTRIBUTES.getStringArray(VALUE_ATTRIBUTE_NAME));
    }

    @Test
    public void testOverride() {
        String propertyNamePrefix = this.strategy.getPropertyNamePrefix(ANNOTATION_CLASS);
        environment.setProperty(propertyNamePrefix + VALUE_ATTRIBUTE_NAME, VALUE_1 + "," + VALUE_2);

        AnnotationAttributes overriddenAttributtes = this.strategy.override(ANNOTATION_CLASS, ATTRIBUTES, null);
        assertArrayEquals(ofArray(VALUE_1, VALUE_2), overriddenAttributtes.getStringArray(VALUE_ATTRIBUTE_NAME));
    }

    @Test
    public void testOverrideOnSameConfigurationProperties() {
        String propertyNamePrefix = this.strategy.getPropertyNamePrefix(ANNOTATION_CLASS);
        environment.setProperty(propertyNamePrefix + VALUE_ATTRIBUTE_NAME, VALUE_1);

        AnnotationAttributes overriddenAttributtes = this.strategy.override(ANNOTATION_CLASS, ATTRIBUTES, null);
        assertArrayEquals(ATTRIBUTES.getStringArray(VALUE_ATTRIBUTE_NAME), overriddenAttributtes.getStringArray(VALUE_ATTRIBUTE_NAME));
    }

    @Test
    public void testOverrideOnDifferentConfigurationProperties() {
        String propertyNamePrefix = this.strategy.getPropertyNamePrefix(ANNOTATION_CLASS);
        environment.setProperty(propertyNamePrefix + "a", VALUE_1);

        AnnotationAttributes overriddenAttributtes = this.strategy.override(ANNOTATION_CLASS, ATTRIBUTES, null);
        assertArrayEquals(ATTRIBUTES.getStringArray(VALUE_ATTRIBUTE_NAME), overriddenAttributtes.getStringArray(VALUE_ATTRIBUTE_NAME));
    }

    @Test
    public void testOverrieOnNoConfigurationProperties() {
        AnnotationAttributes overriddenAttributtes = this.strategy.override(ANNOTATION_CLASS, ATTRIBUTES, null);
        assertSame(ATTRIBUTES, overriddenAttributtes);
    }

    @Test
    public void testOverrideOnInvalidConfigurationProperties() {
        String propertyNamePrefix = this.strategy.getPropertyNamePrefix(ANNOTATION_CLASS);
        environment.setProperty(propertyNamePrefix + FACTORY_ATTRIBUTE_NAME, EMPTY_STRING);

        AnnotationAttributes overriddenAttributtes = this.strategy.override(ANNOTATION_CLASS, ATTRIBUTES, null);
        assertEquals(ATTRIBUTES.getClass(FACTORY_ATTRIBUTE_NAME), overriddenAttributtes.getClass(FACTORY_ATTRIBUTE_NAME));
    }

    @Test
    public void testGetConfigurationProperties() {
        String propertyNamePrefix = this.strategy.getPropertyNamePrefix(ANNOTATION_CLASS);

        environment.setProperty(propertyNamePrefix + "a", "1");
        environment.setProperty(propertyNamePrefix + "b", "2");
        environment.setProperty(propertyNamePrefix + "c", "3");

        Map<String, Object> configurationProperties = this.strategy.getConfigurationProperties(ANNOTATION_CLASS);
        assertEquals(3, configurationProperties.size());
        assertEquals("1", configurationProperties.get("a"));
        assertEquals("2", configurationProperties.get("b"));
        assertEquals("3", configurationProperties.get("c"));
        assertNull(configurationProperties.get("annotationType"));
    }

    @Test
    public void testGetPropertyNamePrefix() {
        String prefixPropertyName = getPrefixPropertyName(ANNOTATION_CLASS);
        this.environment.setProperty(prefixPropertyName, "ms");
        assertEquals("ms.", this.strategy.getPropertyNamePrefix(ANNOTATION_CLASS));
    }

    @Test
    public void testGetPrefixPropertyName() {
        String prefixPropertyName = getPrefixPropertyName(ANNOTATION_CLASS);
        assertEquals("microsphere.spring.prefix." + ANNOTATION_CLASS.getName(), prefixPropertyName);
    }

    @Test
    public void testGetDefaultPropertyNamePrefix() {
        String defaultPropertyNamePrefix = getDefaultPropertyNamePrefix(ANNOTATION_CLASS);
        assertEquals("microsphere.spring.prefix.PropertySource.", defaultPropertyNamePrefix);
    }
}