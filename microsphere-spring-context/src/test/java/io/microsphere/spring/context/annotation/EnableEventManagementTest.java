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
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

/**
 * {@link EnableEventManagement} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        EnableEventManagementTest.class,
        EnableEventManagementTest.Config.class,
        DefaultAdvisorAutoProxyCreator.class
})
@EnableEventManagement(intercepted = true)
public class EnableEventManagementTest {

    private static final Logger logger = LoggerFactory.getLogger(EnableEventManagementTest.class);

    @Bean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
    public static ApplicationEventMulticaster applicationEventMulticaster() {
        return new SimpleApplicationEventMulticaster();
    }

    @Bean
    public static ApplicationEventInterceptor applicationEventInterceptor() {
        return ((event, eventType, chain) -> {
            chain.intercept(event, eventType);
        });
    }

    @Bean
    public static ApplicationListenerInterceptor applicationListenerInterceptor() {
        return ((listener, event, chain) -> {
            chain.intercept(listener, event);
        });
    }

    @Bean
    public StringApplicationListener stringApplicationListener() {
        return new StringApplicationListener();
    }

    static class StringApplicationListener implements ApplicationListener<PayloadApplicationEvent<String>> {

        @Override
        public void onApplicationEvent(PayloadApplicationEvent<String> event) {
            logger.info("The Event : {}", event);
        }
    }

    @Autowired
    private ConfigurableApplicationContext context;

    static class Config {

        @EventListener(PayloadApplicationEvent.class)
        public void onPayloadApplicationEvent(PayloadApplicationEvent<String> event) {
            logger.info("The Event : {}", event);
        }

        @EventListener(String.class)
        public void onPayloadApplicationEvent(String event) {
            logger.info("The Event payload : {}", event);
        }
    }


    @Test
    public void test() {
        AtomicReference<String> eventValueRef = new AtomicReference<>();

        // EventType -> ResolvableType -> PayloadApplicationEvent<String>
        context.addApplicationListener((ApplicationListener<PayloadApplicationEvent<String>>)
                event -> eventValueRef.set(event.getPayload()));

        context.publishEvent("Hello,World");

        assertEquals("Hello,World", eventValueRef.get());
    }
}
