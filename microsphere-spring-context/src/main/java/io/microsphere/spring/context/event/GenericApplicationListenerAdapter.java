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

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.context.event.SmartApplicationListener;

import javax.annotation.Nullable;

import static org.springframework.core.ResolvableType.forClass;

/**
 * The adapter interface of {@link GenericApplicationListener} and {@link SmartApplicationListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
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
