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


import io.microsphere.spring.test.SpringLoggingTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.microsphere.collection.MapUtils.newHashMap;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DefaultPropertiesPropertySourceLoader} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultPropertiesPropertySourceLoader
 * @since 1.0.0
 */
@SpringLoggingTest
class DefaultPropertiesPropertySourceLoaderTest {

    private DefaultPropertiesPropertySourceLoader loader;

    @BeforeEach
    void setUp() {
        this.loader = new DefaultPropertiesPropertySourceLoader();
    }

    @Test
    void testLoadPropertySource() {
        Map<String, Object> defaultProperties = newHashMap();
        loader.loadPropertySource(null, defaultProperties);
        assertTrue(defaultProperties.isEmpty());
    }
}