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
package io.microsphere.spring.context.annotation;

import io.microsphere.spring.context.event.ApplicationEventInterceptor;
import io.microsphere.spring.context.event.ApplicationListenerInterceptor;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

/**
 * {@link EnableEventExtension} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        EnableEventExtensionTest.class,
        EnableEventExtensionTest.Config.class,
        DefaultAdvisorAutoProxyCreator.class
})
@EnableEventExtension(intercepted = true, executorForListener = "taskExecutor"
)
public class EnableEventExtensionTest {

    private static final Logger logger = LoggerFactory.getLogger(EnableEventExtensionTest.class);

    private static AtomicInteger eventValueRef = new AtomicInteger();

    @Bean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
    public static ApplicationEventMulticaster applicationEventMulticaster() {
        return new SimpleApplicationEventMulticaster();
    }

    @Bean
    public static TaskExecutor taskExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean
    public static ApplicationEventInterceptor applicationEventInterceptor() {
        return ((event, eventType, chain) -> {
            if (event instanceof PayloadApplicationEvent) {
                eventValueRef.incrementAndGet();
            }
            chain.intercept(event, eventType);
        });
    }

    @Bean
    public static ApplicationListenerInterceptor applicationListenerInterceptor() {
        return ((listener, event, chain) -> {
            if (event instanceof PayloadApplicationEvent) {
                eventValueRef.incrementAndGet();
                logger.info("listener : {} , event : {} , chain : {}", listener, event, chain);
            }
            chain.intercept(listener, event);
        });
    }

    @Bean
    public static StringApplicationListener stringApplicationListener() {
        return new StringApplicationListener();
    }

    static class StringApplicationListener implements ApplicationListener<PayloadApplicationEvent<String>> {

        @Override
        public void onApplicationEvent(PayloadApplicationEvent<String> event) {
            eventValueRef.incrementAndGet();
        }
    }

    @Autowired
    private ConfigurableApplicationContext context;

    static class Config {

        @EventListener(PayloadApplicationEvent.class)
        public void onPayloadApplicationEvent(PayloadApplicationEvent<String> event) {
            eventValueRef.incrementAndGet();
        }

        @EventListener(String.class)
        public void onPayloadApplicationEvent(String event) {
            eventValueRef.incrementAndGet();
        }
    }

    @After
    public void rest() {
        eventValueRef.set(0);
    }


    @Test
    public void test() {

        // EventType -> ResolvableType -> PayloadApplicationEvent<String>
        context.addApplicationListener((ApplicationListener<PayloadApplicationEvent<String>>)
                event -> {
                    eventValueRef.incrementAndGet();
                });

        context.publishEvent("Hello,World");

        assertEquals(9, eventValueRef.get());
    }
}
