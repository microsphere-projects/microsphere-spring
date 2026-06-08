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

import io.microsphere.collection.SetUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.annotation.Annotation;
import java.util.Set;

import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.spring.core.annotation.GenericAnnotationAttributes.of;
import static io.microsphere.spring.core.annotation.GenericAnnotationAttributes.ofSet;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
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

        AnnotationAttributes annotationAttributes = newAnnotationAttributes();
        attributes = of(annotationAttributes);
        assertAttributes(attributes, contextConfiguration);
    }

    @Test
    public void testOfWithMapAndAnnotationType() {
        GenericAnnotationAttributes attributes = of(getAnnotationAttributes(this.contextConfiguration), ContextConfiguration.class);
        assertAttributes(attributes, contextConfiguration);

        attributes = of(this.attributes, ContextConfiguration.class);
        assertAttributes(attributes, contextConfiguration);
    }

    @Test
    public void testOnInvalidConstructorArgument() {
        assertThrows(IllegalArgumentException.class, () -> of(new AnnotationAttributes()));
        assertThrows(NullPointerException.class, () -> of(null, null));
        assertThrows(IllegalArgumentException.class, () -> of(newHashMap(), null));
    }

    @Test
    public void testOfSet() {
        Set<AnnotationAttributes> annotationAttributesSet = ofSet();
        assertTrue(annotationAttributesSet.isEmpty());

        annotationAttributesSet = ofSet(this.attributes);
        assertEquals(SetUtils.of(this.attributes), annotationAttributesSet);
    }

    @Test
    public void testHashCode() {
        this.attributes.put("a", null);
        assertEquals(this.attributes.hashCode(), of(this.attributes).hashCode());
    }

    @Test
    public void testEquals() {
        AnnotationAttributes annotationAttributes = (AnnotationAttributes) getAnnotationAttributes(this.contextConfiguration);
        assertEquals(this.attributes, annotationAttributes);
    }

    @Test
    public void testEqualsOnNotAnnotationAttributes() {
        assertNotEquals(this.attributes, emptyMap());
    }

    @Test
    public void testEqualsOnDifferentAnnotationType() {
        assertNotEquals(this.attributes, new AnnotationAttributes());
    }

    @Test
    public void testEqualsOnDifferentSize() {
        AnnotationAttributes annotationAttributes = newAnnotationAttributes();
        GenericAnnotationAttributes attributes = of(annotationAttributes);
        attributes.put("a", "1");

        assertNotEquals(this.attributes, attributes);
    }

    @Test
    public void testEqualsOnDifferentEntry() {
        AnnotationAttributes annotationAttributes = newAnnotationAttributes();
        GenericAnnotationAttributes attributes = of(annotationAttributes);
        attributes.put("name", "test");

        assertNotEquals(this.attributes, attributes);
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

    AnnotationAttributes newAnnotationAttributes() {
        AnnotationAttributes annotationAttributes = new AnnotationAttributes(this.attributes.annotationType().getName(), getDefaultClassLoader());
        annotationAttributes.putAll(this.attributes);
        return annotationAttributes;
    }
}