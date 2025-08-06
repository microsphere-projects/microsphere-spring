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

import io.microsphere.annotation.Nullable;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.context.event.SmartApplicationListener;

import static org.springframework.core.ResolvableType.forClass;

/**
 * An adapter interface that combines the functionalities of {@link GenericApplicationListener}
 * and {@link SmartApplicationListener} to provide a more flexible and extensible way to handle
 * application events in the Spring context.
 *
 * <p>Implementing this interface allows a class to be notified of application events while
 * also providing additional control over event and source type filtering, listener ordering,
 * and listener identification.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class MyApplicationListener implements GenericApplicationListenerAdapter {
 *
 *     @Override
 *     public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
 *         return ApplicationReadyEvent.class.isAssignableFrom(eventType);
 *     }
 *
 *     @Override
 *     public boolean supportsSourceType(Class<?> sourceType) {
 *         return sourceType.equals(MyCustomSource.class);
 *     }
 *
 *     @Override
 *     public void onApplicationEvent(ApplicationEvent event) {
 *         // Handle the event here
 *         System.out.println("Received event: " + event);
 *     }
 *
 *     @Override
 *     public int getOrder() {
 *         return 0; // Custom ordering if needed
 *     }
 * }
 * }</pre>
 *
 * <p>This example demonstrates how to implement the {@code GenericApplicationListenerAdapter}
 * to listen for specific application events (e.g., {@code ApplicationReadyEvent}) and filter
 * based on the source type. The listener can also define a custom order to control the sequence
 * of event delivery.</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see GenericApplicationListener
 * @see SmartApplicationListener
 * @since 1.0.0
 */
public interface GenericApplicationListenerAdapter extends GenericApplicationListener, SmartApplicationListener {

    /**
     * {@inheritDoc}
     */
    default boolean supportsSourceType(@Nullable Class<?> sourceType) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    default boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return supportsEventType(forClass(eventType));
    }

    /**
     * {@inheritDoc}
     */
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    /**
     * {@inheritDoc}
     *
     * @since Spring Framework 5.3.5
     */
    default String getListenerId() {
        return "";
    }
}
