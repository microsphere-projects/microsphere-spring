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
package io.microsphere.spring.web.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.util.StreamUtils.copyToString;

/**
 * {@link SmartWebEndpointMappingFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        SmartWebEndpointMappingFactory.class,
        SmartWebEndpointMappingFactoryTest.class
})
class SmartWebEndpointMappingFactoryTest {

    @Value("classpath:META-INF/web-mapping-descriptor.json")
    private Resource fullJsonResource;

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private SmartWebEndpointMappingFactory factory;

    private String fullJson;

    @BeforeEach
    void setUp() throws Throwable {
        this.fullJson = copyToString(this.fullJsonResource.getInputStream(), UTF_8);
    }

    @Test
    void testCreate() {
        assertFactory(factory);
    }

    @Test
    void testCreateWithoutBeanFactory() {
        WebEndpointMappingFactory factory = new SmartWebEndpointMappingFactory(null);
        assertFactory(factory);
    }

    @Test
    void testCreateOnUnsupported() {
        Optional<WebEndpointMapping<Object>> descriptor = factory.create(new Object());
        assertFalse(descriptor.isPresent());
    }

    @Test
    void testCreateOnUnsupportedType() {
        Optional<WebEndpointMapping<Object>> descriptor = factory.create(emptyMap());
        assertFalse(descriptor.isPresent());
    }

    void assertFactory(WebEndpointMappingFactory factory) {
        Optional<WebEndpointMapping> descriptor = factory.create(fullJson);
        assertNotNull(descriptor);
        assertEquals(fullJson, descriptor.get().toJSON());
    }
}
