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


import io.microsphere.logging.Logger;
import io.microsphere.util.ValueHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.context.event.InterceptingApplicationEventMulticasterProxy.RESET_BEAN_NAME_PROPERTY_NAME;
import static io.microsphere.spring.context.event.InterceptingApplicationEventMulticasterProxy.getResetBeanName;
import static io.microsphere.spring.context.event.InterceptingApplicationEventMulticasterProxy.removeApplicationListenersMethod;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

/**
 * {@link InterceptingApplicationEventMulticasterProxy} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see InterceptingApplicationEventMulticasterProxy
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        InterceptingApplicationEventMulticasterProxyTest.class
})
@EnableEventExtension
@DirtiesContext
public class InterceptingApplicationEventMulticasterProxyTest {

    private static final Logger logger = getLogger(InterceptingApplicationEventMulticasterProxyTest.class);

    private static final String TEST_APPLICATION_LISTENER_BEAN_NAME = "testApplicationListener";

    private static final ThreadLocal<ApplicationEvent> eventHolder = new ThreadLocal<>();

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    @Qualifier(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Autowired
    @Qualifier(TEST_APPLICATION_LISTENER_BEAN_NAME)
    private ApplicationListener testApplicationListener;

    private InterceptingApplicationEventMulticasterProxy proxy;

    @Before
    public void setUp() throws Exception {
        this.proxy = (InterceptingApplicationEventMulticasterProxy) applicationEventMulticaster;
        resetEvent();
    }


    @After
    public void tearDown() {
        resetEvent();
    }

    @Test
    public void testGetResetBeanName() {
        MockEnvironment environment = new MockEnvironment();
        assertEquals("applicationEventMulticaster_ORIGINAL", getResetBeanName(environment));

        String resetBeanName = "reset-bean-name";
        environment.setProperty(RESET_BEAN_NAME_PROPERTY_NAME, resetBeanName);
        assertEquals(resetBeanName, getResetBeanName(environment));
    }

    @Test
    public void testApplicationListenerOps() {
        PayloadApplicationEvent<String> event = newEvent("test");

        ValueHolder<ApplicationEvent> eventValueHolder = new ValueHolder<>();

        ApplicationListener listener = e -> {
            eventValueHolder.setValue(e);
        };

        this.proxy.addApplicationListener(listener);

        this.proxy.multicastEvent(event);
        ApplicationEvent currentEvent = getEvent();
        assertSame(event, currentEvent);
        assertSame(event, eventValueHolder.getValue());

        resetEvent();

        PayloadApplicationEvent event2 = newEvent("test2");

        this.proxy.removeApplicationListener(null);
        this.proxy.removeApplicationListener(testApplicationListener);
        this.proxy.removeApplicationListenerBean(TEST_APPLICATION_LISTENER_BEAN_NAME);
        this.proxy.removeApplicationListeners(l -> l == listener);
        this.proxy.removeApplicationListenerBeans(name -> TEST_APPLICATION_LISTENER_BEAN_NAME.equals(name));

        if (removeApplicationListenersMethod == null) {
            this.proxy.removeApplicationListener(listener);
        }

        this.proxy.multicastEvent(event2);
        assertNull(getEvent());
        assertSame(event, eventValueHolder.getValue());

        this.proxy.addApplicationListener(listener);
        this.proxy.multicastEvent(event2);
        assertSame(event2, eventValueHolder.getValue());

        this.proxy.removeAllListeners();

        this.proxy.multicastEvent(event);
        assertSame(event2, eventValueHolder.getValue());
    }

    @Test
    public void testGetDelegate() {
        Object delegate = this.proxy.getDelegate();
        String beanName = getResetBeanName(this.context.getEnvironment());
        ApplicationEventMulticaster bean = this.context.getBean(beanName, ApplicationEventMulticaster.class);
        assertSame(bean, delegate);
    }

    @Test
    public void testWrap() {
        ApplicationListener<?> listener = e -> {
        };
        InterceptingApplicationListener wrapper = this.proxy.wrap(listener);
        assertTrue(this.proxy.isCachedInterceptingApplicationListener(wrapper));
        assertFalse(this.proxy.isCachedInterceptingApplicationListener(listener));

        InterceptingApplicationListener newListener = new InterceptingApplicationListener(listener, emptyList());
        wrapper = this.proxy.wrap(newListener);
        assertTrue(this.proxy.isCachedInterceptingApplicationListener(wrapper));
        assertTrue(this.proxy.isCachedInterceptingApplicationListener(newListener));

        InterceptingApplicationListener newListener2 = new InterceptingApplicationListener(e -> {
        }, emptyList());

        wrapper = this.proxy.wrap(newListener2);
        assertTrue(this.proxy.isCachedInterceptingApplicationListener(wrapper));
        assertTrue(this.proxy.isCachedInterceptingApplicationListener(newListener2));
    }

    <T> PayloadApplicationEvent<T> newEvent(T payload) {
        return new PayloadApplicationEvent<>(this, payload);
    }

    static ApplicationEvent getEvent() {
        return eventHolder.get();
    }

    static void resetEvent() {
        eventHolder.remove();
    }

    @Bean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
    public static ApplicationEventMulticaster applicationEventMulticaster() {
        return new SimpleApplicationEventMulticaster();
    }

    @Bean(TEST_APPLICATION_LISTENER_BEAN_NAME)
    public static ApplicationListener applicationListener() {
        return event -> {
            if (logger.isTraceEnabled()) {
                logger.trace("ApplicationListener is listening event : {}", event);
            }
            eventHolder.set(event);
        };
    }
}