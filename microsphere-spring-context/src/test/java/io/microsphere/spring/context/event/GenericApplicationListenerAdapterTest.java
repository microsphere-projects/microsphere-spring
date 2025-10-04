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
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;
import static org.springframework.core.ResolvableType.forClass;

/**
 * {@link GenericApplicationListenerAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see GenericApplicationListenerAdapter
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(
        classes = {
                GenericApplicationListenerAdapterTest.GenericApplicationListenerAdapterImpl.class
        })
public class GenericApplicationListenerAdapterTest {

    static class GenericApplicationListenerAdapterImpl implements GenericApplicationListenerAdapter {

        @Override
        public boolean supportsEventType(ResolvableType eventType) {
            return true;
        }

        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            assertNotNull(event);
        }
    }

    @Autowired
    private GenericApplicationListenerAdapter listener;

    @Test
    public void testSupportsEventType() {
        assertTrue(listener.supportsEventType(ApplicationEvent.class));
        assertTrue(listener.supportsEventType(forClass(ApplicationEvent.class)));
    }

    @Test
    public void testSupportsSourceType() {
        assertTrue(listener.supportsSourceType(Object.class));
    }

    @Test
    public void testGetOrder() {
        assertEquals(LOWEST_PRECEDENCE, listener.getOrder());
    }

    @Test
    public void testGetListenerId() {
        assertEquals("", listener.getListenerId());
    }
}
