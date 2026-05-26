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
package io.microsphere.spring.config.env.support;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link JsonPropertySourceFactory} Test
 *
 * @see JsonPropertySourceFactory
 * @since 1.0.0
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
class JsonPropertySourceFactoryTest {

    private final JsonPropertySourceFactory factory = new JsonPropertySourceFactory();

    @Test
    void testCreatePropertySource() throws IOException {
        ClassPathResource resource = new ClassPathResource("META-INF/test/json/1.json");
        EncodedResource encodedResource = new EncodedResource(resource);

        PropertySource<?> propertySource = factory.createPropertySource("testJson", encodedResource);

        assertNotNull(propertySource);
        assertInstanceOf(MapPropertySource.class, propertySource);
        assertEquals("testJson", propertySource.getName());
        assertEquals("mercyblitz", propertySource.getProperty("my.name"));
    }

    @Test
    void testCreatePropertySourceWithAlternativeResource() throws IOException {
        ClassPathResource resource = new ClassPathResource("META-INF/test/json/2.json");
        EncodedResource encodedResource = new EncodedResource(resource);

        PropertySource<?> propertySource = factory.createPropertySource("testJson2", encodedResource);

        assertNotNull(propertySource);
        assertEquals("testJson2", propertySource.getName());
        assertEquals("Mercy Ma", propertySource.getProperty("my.name"));
    }
}
