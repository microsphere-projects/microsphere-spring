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

package io.microsphere.spring.test.util;

import io.microsphere.lang.function.ThrowableBiConsumer;
import io.microsphere.lang.function.ThrowableConsumer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Spring Test Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurableApplicationContext
 * @since 1.0.0
 */
public abstract class SpringTestUtils {

    /**
     * Executes the given {@link ThrowableConsumer} within a Spring container context.
     *
     * <p>This method creates a new {@link AnnotationConfigApplicationContext} using the provided
     * configuration classes and passes the context to the consumer for execution.
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Basic usage without any exception
     * SpringTestContext.testInSpringContainer(context -> {
     *     MyService myService = context.getBean(MyService.class);
     *     assertNotNull(myService);
     * }, MyConfig.class);
     *
     * // Example 2: Usage where the consumer throws an exception
     * SpringTestContext.testInSpringContainer(context -> {
     *     throw new RuntimeException("Test Exception");
     * }, MyConfig.class);
     * // The thrown exception will be propagated as a {@link RuntimeException}.
     * }</pre>
     *
     * @param consumer      the instance of {@link ThrowableConsumer} to execute
     * @param configClasses one or more configuration classes to be registered in the Spring context
     * @throws NullPointerException if the given consumer is null
     */
    public static void testInSpringContainer(ThrowableConsumer<ConfigurableApplicationContext> consumer, Class<?>... configClasses) {
        testInSpringContainer((context, environment) -> {
            consumer.accept(context);
        }, configClasses);
    }

    /**
     * Executes the given {@link ThrowableBiConsumer} within a Spring container context.
     *
     * <p>This method creates a new {@link AnnotationConfigApplicationContext} using the provided
     * configuration classes and passes both the context and its environment to the consumer for execution.
     *
     * <h3>Example Usage</h3>
     *
     * <pre>{@code
     * // Example 1: Basic usage without any exception
     * SpringTestContext.testInSpringContainer((context, environment) -> {
     *     MyService myService = context.getBean(MyService.class);
     *     assertNotNull(myService);
     * }, MyConfig.class);
     *
     * // Example 2: Usage where the consumer throws an exception
     * SpringTestContext.testInSpringContainer((context, environment) -> {
     *     throw new RuntimeException("Test Exception");
     * }, MyConfig.class);
     * // The thrown exception will be propagated as a {@link RuntimeException}.
     * }</pre>
     *
     * @param consumer      the instance of {@link ThrowableBiConsumer} to execute
     * @param configClasses one or more configuration classes to be registered in the Spring context
     * @throws NullPointerException if the given consumer is null
     * @see ThrowableBiConsumer
     * @see AnnotationConfigApplicationContext
     */
    public static void testInSpringContainer(ThrowableBiConsumer<ConfigurableApplicationContext, ConfigurableEnvironment> consumer, Class<?>... configClasses) {
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(configClasses);
        try {
            consumer.accept(context, context.getEnvironment());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        context.close();
    }

    private SpringTestUtils() {
    }
}
