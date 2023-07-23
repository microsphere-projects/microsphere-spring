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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.PostConstruct;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.util.StreamUtils.copyToString;

/**
 * {@link SmartWebMappingDescriptorFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SmartWebMappingDescriptorFactoryTest.class)
public class SmartWebMappingDescriptorFactoryTest {

    private WebMappingDescriptorFactory factory = new SmartWebMappingDescriptorFactory();

    @Value("classpath:META-INF/web-mapping-descriptor.json")
    private Resource fullJsonResource;

    private String fullJson;

    @PostConstruct
    public void init() throws Throwable {
        this.fullJson = copyToString(this.fullJsonResource.getInputStream(), UTF_8);
    }

    @Test
    public void testCreate() {
        WebMappingDescriptor descriptor = factory.create(fullJson);
        assertNotNull(descriptor);
        assertEquals(fullJson, descriptor.toJSON());
    }
}
