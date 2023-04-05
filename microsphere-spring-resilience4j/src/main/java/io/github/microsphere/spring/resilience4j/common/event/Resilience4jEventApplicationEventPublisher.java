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
package io.github.microsphere.spring.resilience4j.common.event;

import io.github.resilience4j.core.EventConsumer;
import io.github.resilience4j.core.EventPublisher;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

/**
 * Propagating Resilience4j's event to the Spring {@link ApplicationEvent}
 *
 * @param <E>  the type of Resilience4j entry
 * @param <ET> the type o Resilience4j entries' event
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class Resilience4jEventApplicationEventPublisher<E, ET> implements EventConsumer<ET>, RegistryEventConsumer<E>, ApplicationEventPublisherAware {

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void consumeEvent(ET event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void onEntryAddedEvent(EntryAddedEvent<E> entryAddedEvent) {
        register(entryAddedEvent::getAddedEntry);
    }

    @Override
    public void onEntryRemovedEvent(EntryRemovedEvent<E> entryRemoveEvent) {
    }

    @Override
    public void onEntryReplacedEvent(EntryReplacedEvent<E> entryReplacedEvent) {
        register(entryReplacedEvent::getNewEntry);
    }

    private void register(Supplier<E> entrySupplier) {
        E entry = entrySupplier.get();
        Class<?> entryClass = entry.getClass();
        Method eventPublisherMethod = findMethod(entryClass, "getEventPublisher");
        EventPublisher<ET> eventPublisher = (EventPublisher) invokeMethod(eventPublisherMethod, entry);
        eventPublisher.onEvent(this::consumeEvent);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
