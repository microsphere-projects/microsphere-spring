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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.beans.ConstructorProperties;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.reflect.ConstructorUtils.findConstructor;
import static io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes.of;
import static io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes.ofSet;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.Assert.assertEquals;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * {@link ResolvablePlaceholderAnnotationAttributes} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ResolvablePlaceholderAnnotationAttributesTest.class})
@TestPropertySource(properties = {"a=1", "b=2"})
public class ResolvablePlaceholderAnnotationAttributesTest {

    @Autowired
    private Environment environment;

    @ConstructorProperties({"${a}", "${b}"})
    public ResolvablePlaceholderAnnotationAttributesTest() {
    }

    @Test
    public void testOfWithAnnotation() {
        ConstructorProperties constructorProperties = findConstructor(getClass()).getAnnotation(ConstructorProperties.class);
        ResolvablePlaceholderAnnotationAttributes annotationAttributes = of(constructorProperties, environment);
        assertEquals(ConstructorProperties.class, annotationAttributes.annotationType());
        assertEquals(constructorProperties.annotationType(), annotationAttributes.annotationType());
        String[] values = annotationAttributes.getStringArray("value");
        assertEquals("1", values[0]);
        assertEquals("2", values[1]);
    }

    @Test
    public void testOfWithAnnotationAttributes() {
        Map<String, Object> map = ofMap("a", "${a}", "b", "${b}");
        AnnotationAttributes annotationAttributes = fromMap(map);
        ResolvablePlaceholderAnnotationAttributes attributes = of(annotationAttributes, ConstructorProperties.class, this.environment);
        assertAnnotationAttributes(attributes);

        attributes = of(attributes, this.environment);
        assertAnnotationAttributes(attributes);
    }

    @Test
    public void testOfWithMap() {
        Map<String, Object> map = ofMap("a", "${a}", "b", "${b}");
        ResolvablePlaceholderAnnotationAttributes attributes = of(map, ConstructorProperties.class, this.environment);
        assertAnnotationAttributes(attributes);

        attributes = of(attributes, ConstructorProperties.class, this.environment);
        assertAnnotationAttributes(attributes);
    }

    @Test
    public void testOfSet() {
        Map<String, Object> map = ofMap("a", "${a}", "b", "${b}");
        Set<AnnotationAttributes> attributesSet = ofSet(ofArray(of(map, ConstructorProperties.class, this.environment)), this.environment);
        assertEquals(1, attributesSet.size());
        AnnotationAttributes attributes = attributesSet.iterator().next();
        assertAnnotationAttributes(attributes);
    }

    @Test
    public void testOfSetOnEmptyArray() {
        Set<AnnotationAttributes> attributesSet = ofSet(ofArray(), this.environment);
        assertEquals(0, attributesSet.size());
    }

    private void assertAnnotationAttributes(AnnotationAttributes attributes) {
        assertEquals(2, attributes.size());
        assertEquals("1", attributes.getString("a"));
        assertEquals("2", attributes.getString("b"));
    }

}
