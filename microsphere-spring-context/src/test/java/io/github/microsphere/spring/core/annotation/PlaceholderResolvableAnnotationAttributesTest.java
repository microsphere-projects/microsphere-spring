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
package io.github.microsphere.spring.core.annotation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.beans.ConstructorProperties;

import static org.junit.Assert.assertEquals;

/**
 * {@link PlaceholderResolvableAnnotationAttributes} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {PlaceholderResolvableAnnotationAttributesTest.class})
@TestPropertySource(properties = {"a=1", "b=2"})
public class PlaceholderResolvableAnnotationAttributesTest {

    @Autowired
    private Environment environment;

    @ConstructorProperties({"${a}", "${b}"})
    public PlaceholderResolvableAnnotationAttributesTest() {
    }

    @Test
    public void test() throws NoSuchMethodException {
        ConstructorProperties constructorProperties = PlaceholderResolvableAnnotationAttributesTest.class.getConstructor().getAnnotation(ConstructorProperties.class);
        PlaceholderResolvableAnnotationAttributes annotationAttributes = new PlaceholderResolvableAnnotationAttributes(constructorProperties, environment);
        assertEquals(ConstructorProperties.class, annotationAttributes.annotationType());
        assertEquals(constructorProperties.annotationType(), annotationAttributes.annotationType());
        String[] values = annotationAttributes.getStringArray("value");
        assertEquals("1", values[0]);
        assertEquals("2", values[1]);
    }
}
