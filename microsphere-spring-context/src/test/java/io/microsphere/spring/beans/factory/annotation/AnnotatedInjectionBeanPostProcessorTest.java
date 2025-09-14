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

import io.microsphere.spring.test.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.microsphere.spring.beans.factory.annotation.AnnotatedInjectionBeanPostProcessorTest.TestConfiguration.Child;
import static io.microsphere.spring.beans.factory.annotation.AnnotatedInjectionBeanPostProcessorTest.TestConfiguration.Parent;
import static io.microsphere.spring.beans.factory.annotation.AnnotatedInjectionBeanPostProcessorTest.TestConfiguration.UserHolder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AnnotatedInjectionBeanPostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        AnnotatedInjectionBeanPostProcessorTest.TestConfiguration.class,
        ReferencedInjectedBeanPostProcessor.class,
        AnnotatedInjectionBeanPostProcessorTest.GenericConfiguration.class,
})
@SuppressWarnings({"deprecation", "unchecked"})
class AnnotatedInjectionBeanPostProcessorTest {

    @Autowired
    @Qualifier("parent")
    private Parent parent;

    @Autowired
    @Qualifier("child")
    private Child child;

    @Autowired
    private UserHolder userHolder;

    @Autowired
    private AnnotatedInjectionBeanPostProcessor processor;

    @Autowired
    private Environment environment;

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private GenericChild genericChild;

    @Test
    void testCustomizedAnnotationBeanPostProcessor() {

        assertEquals(environment, processor.getEnvironment());
        assertEquals(beanFactory.getBeanClassLoader(), processor.getClassLoader());
        assertEquals(beanFactory, processor.getBeanFactory());

        assertEquals(1, processor.getAnnotationTypes().size());
        assertTrue(processor.getAnnotationTypes().contains(Referenced.class));
        assertEquals(Ordered.LOWEST_PRECEDENCE - 3, processor.getOrder());
    }

    @Test
    void testReferencedUser() {
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

    @Import(GenericChild.class)
    public static class GenericConfiguration {

    }

    static class TestConfiguration {

        static class Parent {

            // Case : inject to field
            @Referenced
            User parentUser;

            User user;

            // Case : inject to method
            @Referenced
            public void setUser(User user) {
                this.user = user;
            }

            // Case : inject to method without parameter(invalid)
            @Referenced
            public void action() {
            }
        }

        static class Child extends Parent {

            // Case : inject to field
            @Referenced
            User childUser;

            /**
             * Case : inject to static field(invalid)
             */
            @Referenced
            static User invalidUser;

            /**
             * Case : inject to static method with parameter(invalid)
             */
            @Referenced
            static void init(User user) {
            }
        }

        static class UserHolder {

            User user;
        }


        @Bean
        public Parent parent() {
            return new Parent();
        }

        @Bean
        public Child child() {
            return new Child();
        }


        @Bean
        public User user() {
            User user = new User();
            user.setName("mercyblitz");
            user.setAge(32);
            return user;
        }

        @Bean
        public UserHolder userHolder(User user) {
            UserHolder userHolder = new UserHolder();
            userHolder.user = user;
            return userHolder;
        }

    }


    static abstract class GenericParent<S> {

        // Case : inject to field with generic type
        @Referenced
        S s;

        S s1;

        S s2;

        // Case : inject to method with generic type parameters
        @Referenced
        public void init(S s1, S s2) {
            this.s1 = s1;
            this.s2 = s2;
        }
    }

    static class GenericChild extends GenericParent<User> {

        private final User user;

        // Case : inject to constructor with parameter
        @Referenced
        public GenericChild(@Referenced User user) {
            this.user = user;
        }

    }
}
