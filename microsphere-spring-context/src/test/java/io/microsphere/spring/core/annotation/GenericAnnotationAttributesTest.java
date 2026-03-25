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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static io.microsphere.spring.core.annotation.GenericAnnotationAttributes.of;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;

/**
 * {@link GenericAnnotationAttributes} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {GenericAnnotationAttributesTest.class})
public class GenericAnnotationAttributesTest {

    private ContextConfiguration contextConfiguration;

    private GenericAnnotationAttributes attributes;

    @Before
    public void setUp() {
        this.contextConfiguration = GenericAnnotationAttributesTest.class.getAnnotation(ContextConfiguration.class);
        this.attributes = of(this.contextConfiguration);
    }

    @Test
    public void testOfWithAnnotation() {
        assertAttributes(attributes, contextConfiguration);
    }

    @Test
    public void testOfWithAnnotationAttributes() {
        GenericAnnotationAttributes attributes = of(this.attributes);
        assertAttributes(attributes, contextConfiguration);
    }

    @Test
    public void testHashCode() {
        assertEquals(this.attributes.hashCode(), of(this.attributes).hashCode());
    }

    @Test
    public void testEquals() {
        AnnotationAttributes annotationAttributes = (AnnotationAttributes) getAnnotationAttributes(this.contextConfiguration);
        assertEquals(this.attributes, annotationAttributes);
    }

    @Test
    public void testToString() {
        assertNotNull(this.attributes.toString());
    }

    void assertAttributes(GenericAnnotationAttributes attributes, ContextConfiguration contextConfiguration) {
        assertEquals(ContextConfiguration.class, attributes.annotationType());
        assertEquals(contextConfiguration.annotationType(), attributes.annotationType());
        assertArrayEquals(contextConfiguration.initializers(), attributes.getClassArray("initializers"));
        assertArrayEquals(contextConfiguration.classes(), attributes.getClassArray("classes"));
        assertArrayEquals(contextConfiguration.locations(), attributes.getStringArray("locations"));
        assertEquals(contextConfiguration.name(), attributes.getString("name"));
        assertEquals(contextConfiguration.loader(), attributes.getClass("loader"));
        assertEquals(contextConfiguration.inheritInitializers(), attributes.getBoolean("inheritInitializers"));
        assertEquals(contextConfiguration.inheritLocations(), attributes.getBoolean("inheritLocations"));
        assertArrayEquals(contextConfiguration.value(), attributes.getStringArray("value"));
    }
}
