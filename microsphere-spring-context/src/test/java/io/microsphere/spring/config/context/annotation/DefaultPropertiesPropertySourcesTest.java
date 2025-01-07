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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static io.microsphere.spring.core.env.PropertySourcesUtils.DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link DefaultPropertiesPropertySources} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DefaultPropertiesPropertySources
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DefaultPropertiesPropertySourcesTest.class})
@DefaultPropertiesPropertySource(value = {"classpath*:/META-INF/test/a.properties"})
@DefaultPropertiesPropertySource(value = {"classpath*:/META-INF/test/b.properties"})
public class DefaultPropertiesPropertySourcesTest {

    @Autowired
    private ConfigurableEnvironment environment;

    private MutablePropertySources propertySources;

    @Before
    public void before() {
        this.propertySources = environment.getPropertySources();
    }

    @Test
    public void test() {
        assertTrue(propertySources.contains(DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME));
        assertEquals("1", environment.getProperty("a"));
        assertEquals("2", environment.getProperty("b"));
    }
}
