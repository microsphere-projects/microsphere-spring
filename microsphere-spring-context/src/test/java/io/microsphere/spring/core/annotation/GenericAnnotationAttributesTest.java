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
package io.microsphere.spring.core.annotation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static io.microsphere.spring.core.annotation.GenericAnnotationAttributes.of;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * {@link GenericAnnotationAttributes} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {GenericAnnotationAttributesTest.class})
public class GenericAnnotationAttributesTest {

    @Test
    public void test() {
        ContextConfiguration contextConfiguration = GenericAnnotationAttributesTest.class.getAnnotation(ContextConfiguration.class);
        GenericAnnotationAttributes annotationAttributes = of(contextConfiguration);
        assertEquals(ContextConfiguration.class, annotationAttributes.annotationType());
        assertEquals(contextConfiguration.annotationType(), annotationAttributes.annotationType());
        assertArrayEquals(contextConfiguration.initializers(), annotationAttributes.getClassArray("initializers"));
        assertArrayEquals(contextConfiguration.classes(), annotationAttributes.getClassArray("classes"));
        assertArrayEquals(contextConfiguration.locations(), annotationAttributes.getStringArray("locations"));
        assertEquals(contextConfiguration.name(), annotationAttributes.getString("name"));
        assertEquals(contextConfiguration.loader(), annotationAttributes.getClass("loader"));
        assertEquals(contextConfiguration.inheritInitializers(), annotationAttributes.getBoolean("inheritInitializers"));
        assertEquals(contextConfiguration.inheritLocations(), annotationAttributes.getBoolean("inheritLocations"));
        assertEquals(contextConfiguration.value(), annotationAttributes.getStringArray("value"));

    }
}
