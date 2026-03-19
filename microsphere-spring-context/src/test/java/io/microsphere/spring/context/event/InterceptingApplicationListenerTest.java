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


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link InterceptingApplicationListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see InterceptingApplicationListener
 * @since 1.0.0
 */
class InterceptingApplicationListenerTest {

    private static final ThreadLocal<ApplicationEvent> eventHolder = new ThreadLocal<>();

    private ApplicationListener listener;

    private InterceptingApplicationListener interceptingApplicationListener;

    @BeforeEach
    void setUp() {
        this.listener = e -> {
            eventHolder.set(e);
        };
        this.interceptingApplicationListener = new InterceptingApplicationListener(this.listener, emptyList());
    }

    @AfterEach
    void tearDown() {
        eventHolder.remove();
    }

    @Test
    void testConstructor() {
        GenericApplicationListener genericApplicationListener = new GenericApplicationListener() {
            @Override
            public void onApplicationEvent(ApplicationEvent event) {
                listener.onApplicationEvent(event);
            }

            @Override
            public boolean supportsEventType(ResolvableType eventType) {
                return true;
            }
        };
        assertNotNull(new InterceptingApplicationListener(genericApplicationListener, emptyList()));
    }

    @Test
    void testSupportsEventType() {
        assertTrue(this.interceptingApplicationListener.supportsEventType(ApplicationEvent.class));
    }

    @Test
    void testOnApplicationEvent() {
        PayloadApplicationEvent event = new PayloadApplicationEvent(this, "test");
        this.interceptingApplicationListener.onApplicationEvent(event);
        assertSame(event, eventHolder.get());
    }

    @Test
    void testGetDelegate() {
        assertSame(this.listener, this.interceptingApplicationListener.getDelegate());
    }

    @Test
    void testEquals() {
        assertTrue(this.interceptingApplicationListener.equals(this.interceptingApplicationListener));
        assertTrue(this.interceptingApplicationListener.equals(new InterceptingApplicationListener(this.interceptingApplicationListener, emptyList())));
        assertFalse(this.interceptingApplicationListener.equals(this.listener));
    }

    @Test
    void testHashCode() {
        assertEquals(this.interceptingApplicationListener.hashCode(), this.interceptingApplicationListener.hashCode());
        assertSame(this.listener, this.interceptingApplicationListener.getDelegate());
        assertEquals(this.interceptingApplicationListener.hashCode(), this.listener.hashCode());
    }
}