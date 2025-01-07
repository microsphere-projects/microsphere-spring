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

import io.microsphere.spring.util.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AnnotatedInjectionBeanPostProcessor} Test
 *
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        AnnotationInjectedBeanPostProcessorTest.TestConfiguration.class,
        AnnotatedInjectionBeanPostProcessorTest.ReferencedInjectedBeanPostProcessor.class,
        AnnotatedInjectionBeanPostProcessorTest.GenericConfiguration.class,
})
@SuppressWarnings({"deprecation", "unchecked"})
public class AnnotatedInjectionBeanPostProcessorTest {

    @Autowired
    @Qualifier("parent")
    private AnnotationInjectedBeanPostProcessorTest.TestConfiguration.Parent parent;

    @Autowired
    @Qualifier("child")
    private AnnotationInjectedBeanPostProcessorTest.TestConfiguration.Child child;

    @Autowired
    private AnnotationInjectedBeanPostProcessorTest.TestConfiguration.UserHolder userHolder;

    @Autowired
    private AnnotatedInjectionBeanPostProcessor processor;

    @Autowired
    private Environment environment;

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private GenericChild genericChild;

    @Test
    public void testCustomizedAnnotationBeanPostProcessor() {

        assertEquals(environment, processor.getEnvironment());
        assertEquals(beanFactory.getBeanClassLoader(), processor.getClassLoader());
        assertEquals(beanFactory, processor.getBeanFactory());

        assertEquals(1, processor.getAnnotationTypes().size());
        assertTrue(processor.getAnnotationTypes().contains(Referenced.class));
        assertEquals(Ordered.LOWEST_PRECEDENCE - 3, processor.getOrder());
    }

    @Test
    public void testReferencedUser() {
        assertEquals("mercyblitz", parent.user.getName());
        assertEquals(32, parent.user.getAge());
        assertEquals(parent.user, parent.parentUser);
        assertEquals(parent.user, child.childUser);
        assertEquals(parent.user, userHolder.user);
        assertEquals(parent.user, genericChild.s);
        assertEquals(parent.user, genericChild.s1);
        assertEquals(parent.user, genericChild.s2);
        assertEquals(parent.user, child.user);
    }

    public static class ReferencedInjectedBeanPostProcessor extends AnnotatedInjectionBeanPostProcessor {

        public ReferencedInjectedBeanPostProcessor() {
            super(Referenced.class);
        }

    }

    @Import(GenericChild.class)
    public static class GenericConfiguration {

    }


    static abstract class GenericParent<S> {

        @Referenced
        S s;

        S s1;

        S s2;

        @Referenced
        public void init(S s1, S s2) {
            this.s1 = s1;
            this.s2 = s2;
        }
    }

    static class GenericChild extends GenericParent<User> {

        private final User user;

        @Referenced
        public GenericChild(@Referenced User user) {
            this.user = user;
        }

    }
}
