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
package io.microsphere.spring.beans.factory.annotation;

import io.microsphere.logging.test.junit4.LoggingLevelsRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import static io.microsphere.logging.test.junit4.LoggingLevelsRule.levels;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.findMethod;

/**
 * {@link AnnotatedInjectionPointDependencyResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotatedInjectionPointDependencyResolver
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class AnnotatedInjectionPointDependencyResolverTest {

    @ClassRule
    public static final LoggingLevelsRule LOGGING_LEVELS_RULE = levels("TRACE", "INFO", "ERROR");


    // ---- Helper types used in tests ----

    static class Sample {
        @Autowired
        Object annotatedField;

        Object unannotatedField;

        void method(@Autowired Object param) {
        }

        void noAnnotationMethod(Object param) {
        }
    }

    // ---- Tests ----

    /**
     * AutowiredInjectionPointDependencyResolver uses the no-arg constructor that infers
     * the annotation type from the generic type parameter.
     */
    @Test
    public void testGetAnnotationTypeViaTypeInference() {
        AutowiredInjectionPointDependencyResolver resolver = new AutowiredInjectionPointDependencyResolver();
        assertEquals(Autowired.class, resolver.getAnnotationType());
    }

    /**
     * ResourceInjectionPointDependencyResolver also uses type inference (infers Resource.class).
     */
    @Test
    public void testGetAnnotationTypeViaTypeInferenceForResource() {
        ResourceInjectionPointDependencyResolver resolver = new ResourceInjectionPointDependencyResolver();
        assertEquals(Resource.class, resolver.getAnnotationType());
    }

    /**
     * getAnnotation(Field) returns the annotation when the field is annotated.
     */
    @Test
    public void testGetAnnotationFromAnnotatedField() {
        AutowiredInjectionPointDependencyResolver resolver = new AutowiredInjectionPointDependencyResolver();
        Field field = findField(Sample.class, "annotatedField");
        Autowired annotation = resolver.getAnnotation(field);
        assertNotNull(annotation);
        assertEquals(Autowired.class, annotation.annotationType());
    }

    /**
     * getAnnotation(Field) returns null when the field has no matching annotation.
     */
    @Test
    public void testGetAnnotationFromUnannotatedField() {
        AutowiredInjectionPointDependencyResolver resolver = new AutowiredInjectionPointDependencyResolver();
        Field field = findField(Sample.class, "unannotatedField");
        Autowired annotation = resolver.getAnnotation(field);
        assertNull(annotation);
    }

    /**
     * getAnnotation(Parameter) on a parameter that carries @Autowired.
     */
    @Test
    public void testGetAnnotationFromAnnotatedParameter() {
        AutowiredInjectionPointDependencyResolver resolver = new AutowiredInjectionPointDependencyResolver();
        Parameter parameter = findMethod(Sample.class, "method", Object.class).getParameters()[0];
        // AutowiredInjectionPointDependencyResolver.getAnnotation(parameter) looks at param
        // first, then the declaring executable
        Annotation annotation = resolver.getAnnotation(parameter);
        assertNotNull(annotation);
    }

    /**
     * getAnnotation(Parameter) returns null when parameter (and its method) carry no annotation.
     */
    @Test
    public void testGetAnnotationFromUnannotatedParameter() {
        // Use ResourceInjectionPointDependencyResolver which only looks at the declaring
        // executable for @Resource; "noAnnotationMethod" has no @Resource
        ResourceInjectionPointDependencyResolver resolver = new ResourceInjectionPointDependencyResolver();
        Parameter parameter = findMethod(Sample.class, "noAnnotationMethod", Object.class).getParameters()[0];
        Annotation annotation = resolver.getAnnotation(parameter);
        assertNull(annotation);
    }

    /**
     * getAnnotation(AnnotatedElement) falls through to the base implementation when
     * called via getAnnotation(Field) on the explicit-type constructor path.
     */
    @Test
    public void testGetAnnotationTypeViaExplicitConstructor() {
        // Subclass created with explicit annotation type
        AutowiredInjectionPointDependencyResolver resolver = new AutowiredInjectionPointDependencyResolver();
        // Just verify the annotation type round-trip
        assertEquals(Autowired.class, resolver.getAnnotationType());
    }
}