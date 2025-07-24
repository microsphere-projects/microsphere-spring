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

package io.microsphere.spring.test.context.annotation;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * {@link AnnotatedTypeMetadataTestFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotatedTypeMetadataTestFactory
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        AnnotatedTypeMetadataTestFactory.class,
        AnnotatedTypeMetadataTestFactoryTest.class
})
public class AnnotatedTypeMetadataTestFactoryTest {

    @Autowired
    private AnnotatedTypeMetadataTestFactory factory;

    @Test
    public void testCreateMethodAnnotatedTypeMetadata() {
        AnnotatedTypeMetadata metadata = factory.createMethodAnnotatedTypeMetadata();
        assertNotNull(metadata);
        assertEquals("testCreateMethodAnnotatedTypeMetadata", ((StandardMethodMetadata) metadata).getMethodName());
    }
}