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
package io.microsphere.spring.guice.annotation;

import com.google.inject.Inject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A Guice {@link Inject} annotation without {@code optional = true} declares a
 * <b>required</b> dependency, thus a missing dependency must fail the startup of
 * the {@link org.springframework.context.ApplicationContext}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableGuice
 * @since 1.0.0
 */
class EnableGuiceRequiredInjectionTest {

    @Test
    void testRequiredDependencyMissing() {
        assertThrows(BeanCreationException.class, () ->
                new AnnotationConfigApplicationContext(MissingDependencyConfiguration.class));
    }

    @EnableGuice
    @Configuration
    static class MissingDependencyConfiguration {

        /**
         * No bean of type {@link Long} is registered in the context, and this
         * plain {@link Inject} (optional defaults to false) is required
         */
        @Inject
        private Long missingDependency;
    }
}
