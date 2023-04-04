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
package io.github.microsphere.spring.resilience4j.circuitbreaker.event;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

import java.util.function.Supplier;

/**
 * The {@link CircuitBreakerEvent CircuitBreakerEvents'} {@link io.github.resilience4j.core.EventConsumer consumer beans} register
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class CircuitBreakerEventConsumerRegistrar implements RegistryEventConsumer<CircuitBreaker> {

    private final BeanFactory beanFactory;

    public CircuitBreakerEventConsumerRegistrar(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
        register(entryAddedEvent::getAddedEntry);
    }

    @Override
    public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
    }

    @Override
    public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
        register(entryReplacedEvent::getNewEntry);
    }

    private void register(Supplier<CircuitBreaker> circuitBreakerSupplier) {
        CircuitBreaker circuitBreaker = circuitBreakerSupplier.get();
        CircuitBreaker.EventPublisher eventPublisher = circuitBreaker.getEventPublisher();
        ReflectionUtils.doWithLocalMethods(CircuitBreaker.EventPublisher.class, method -> {
            if (method.getParameterCount() == 1) {
                method.setAccessible(true);
                ResolvableType type = ResolvableType.forMethodParameter(method, 0);
                ObjectProvider objectProvider = beanFactory.getBeanProvider(type);
                objectProvider.forEach(eventConsumerBean -> ReflectionUtils.invokeMethod(method, eventPublisher, eventConsumerBean));
            }
        });
    }
}
