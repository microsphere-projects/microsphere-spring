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

package io.microsphere.spring.context.event;


import io.microsphere.lang.MutableInteger;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.function.Consumer;

import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link EventExtensionRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EventExtensionRegistrar
 * @since 1.0.0
 */
class EventExtensionRegistrarTest {

    static class ExecutorConfig {
        @Bean
        public TaskExecutor taskExecutor() {
            return new SyncTaskExecutor();
        }
    }

    static class InterceptorConfig {
        @Bean
        public ApplicationEventInterceptor applicationEventInterceptor() {
            return ((event, eventType, chain) -> {
                if (event instanceof PayloadApplicationEvent) {
                    PayloadApplicationEvent pe = (PayloadApplicationEvent) event;
                    MutableInteger i = (MutableInteger) pe.getPayload();
                    i.incrementAndGet();
                }
                chain.intercept(event, eventType);
            });
        }
    }

    @EnableEventExtension
    static class DefaultConfig {
    }

    @EnableEventExtension(executorForListener = "taskExecutor")
    static class FullConfig {
    }

    @EnableEventExtension(intercepted = false, executorForListener = "taskExecutor")
    static class NoInterceptedConfig {
    }

    @EnableEventExtension(intercepted = false)
    static class NoSenseConfig {
    }

    @EnableEventExtension
    static class DuplicatedDefaultConfig {
    }

    static class MutableIntegerApplicationListener implements ApplicationListener<PayloadApplicationEvent<MutableInteger>> {

        @Override
        public void onApplicationEvent(PayloadApplicationEvent<MutableInteger> event) {
            MutableInteger mutableInteger = event.getPayload();
            mutableInteger.incrementAndGet();
        }
    }

    @Test
    void testDefaultConfig() {
        test(2, DefaultConfig.class);
    }

    @Test
    void testFullConfig() {
        test(2, FullConfig.class);
    }

    @Test
    void testNoInterceptedConfig() {
        test(1, NoInterceptedConfig.class);
    }

    @Test
    void testNoSenseConfig() {
        test(1, NoSenseConfig.class);
    }

    @Test
    void testDuplicatedConfigs() {
        test(2, DefaultConfig.class, DuplicatedDefaultConfig.class);
    }

    @Test
    void testRebuildConfigs() {
        test(3, DefaultConfig.class, FullConfig.class);
    }

    void test(int expected, Class<?>... configClasses) {
        MutableInteger i = new MutableInteger(0);
        testInContext(context -> {
            context.addApplicationListener(new MutableIntegerApplicationListener());
            context.publishEvent(i);
        }, configClasses);

        assertEquals(expected, i.get());
    }

    void testInContext(Consumer<ConfigurableApplicationContext> contextConsumer, Class<?>... configClasses) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        Class<?>[] requiredConfigClasses = ofArray(InterceptorConfig.class, ExecutorConfig.class);
        context.register(requiredConfigClasses);
        context.register(configClasses);
        context.refresh();
        contextConsumer.accept(context);
        context.close();
    }
}