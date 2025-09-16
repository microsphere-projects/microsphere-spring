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

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A container annotation that holds multiple {@link EnableConfigurationBeanBinding} annotations.
 * It is used to apply several configuration bean bindings at once.
 *
 * <h3>Example Usage</h3>
 *
 * <h4>Basic Usage</h4>
 * <pre>{@code
 * @EnableConfigurationBeanBindings(value = {
 *     @EnableConfigurationBeanBinding(name = "myBean1", value = MyBean1.class),
 *     @EnableConfigurationBeanBinding(name = "myBean2", value = MyBean2.class)
 * })
 * public class MyConfiguration {
 * }
 * }</pre>
 *
 * <h4>Java 8+ {@link Repeatable @Repeatable} Usage</h4>
 * <pre>{@code
 * @EnableConfigurationBeanBinding(name = "myBean1", value = MyBean1.class)
 * @EnableConfigurationBeanBinding(name = "myBean2", value = MyBean2.class)
 * public class MyConfiguration {
 * }
 * }</pre
 *
 * <p>The above example will register two configuration beans with names "myBean1" and "myBean2"
 * bound to their respective classes.</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Import(ConfigurationBeanBindingsRegister.class)
public @interface EnableConfigurationBeanBindings {

    /**
     * @return the array of {@link EnableConfigurationBeanBinding EnableConfigurationBeanBindings}
     */
    EnableConfigurationBeanBinding[] value();
}
