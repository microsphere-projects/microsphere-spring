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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

/**
 * {@link InterceptingApplicationEventMulticaster} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        InterceptingApplicationEventMulticasterTest.class
})
public class InterceptingApplicationEventMulticasterTest {

    @Bean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
    public static ApplicationEventMulticaster applicationEventMulticaster() {
        return new InterceptingApplicationEventMulticaster();
    }

    @Bean
    public static ApplicationEventInterceptor applicationEventInterceptor() {
        return ((event, eventType, chain) -> {
            chain.doIntercept(event, eventType);
        });
    }

    @Bean
    public static ApplicationListenerInterceptor applicationListenerInterceptor() {
        return ((listener, event, chain) -> {
            chain.doIntercept(listener, event);
        });
    }

    @Autowired
    private ConfigurableApplicationContext context;


    @Test
    public void test() {
        AtomicReference<String> eventValueRef = new AtomicReference<>();

        context.addApplicationListener((ApplicationListener<PayloadApplicationEvent<String>>)
                event -> eventValueRef.set(event.getPayload()));
        
        context.publishEvent("Hello,World");

        assertEquals("Hello,World", eventValueRef.get());
    }
}
