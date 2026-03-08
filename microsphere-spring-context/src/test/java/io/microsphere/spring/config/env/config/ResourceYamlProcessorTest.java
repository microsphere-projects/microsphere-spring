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

package io.microsphere.spring.config.env.config;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ResourceYamlProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourceYamlProcessor
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ResourceYamlProcessorTest.class)
class ResourceYamlProcessorTest {

    @Value("META-INF/test/yaml/1.yaml")
    private Resource yamlResource1;

    @Test
    void testProcess() {
        ResourceYamlProcessor processor = new ResourceYamlProcessor(yamlResource1);
        assertProcess(processor, "mercyblitz");
    }

    void assertProcess(ResourceYamlProcessor processor, String expectedProeprtyValue) {
        Map<String, Object> properties = processor.process();
        assertEquals(1, properties.size());
        assertEquals(expectedProeprtyValue, properties.get("my.name"));
    }
}