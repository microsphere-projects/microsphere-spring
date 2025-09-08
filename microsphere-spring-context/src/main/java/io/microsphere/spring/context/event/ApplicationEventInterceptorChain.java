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
import org.springframework.core.ResolvableType;

/**
 * A chain of {@link ApplicationEventInterceptor} instances that can be used to apply
 * cross-cutting logic before and after the processing of an {@link ApplicationEvent}.
 *
 * <p>{@link ApplicationEventInterceptor} implementations can perform actions such as:
 * <ul>
 *     <li>Measuring the time taken to process an event</li>
 *     <li>Adding contextual information before event processing</li>
 *     <li>Performing cleanup or post-processing after event handling</li>
 * </ul>
 *
 * <p>Interceptors in the chain are typically ordered, and each interceptor decides whether to
 * pass the event along to the next interceptor in the chain by calling
 * {@link ApplicationEventInterceptorChain#intercept(ApplicationEvent, ResolvableType)}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class LoggingInterceptor implements ApplicationEventInterceptor {
 *     public void intercept(ApplicationEvent event, ResolvableType eventType, ApplicationEventInterceptorChain chain) {
 *         System.out.println("Before processing event: " + event.getClass().getSimpleName());
 *         chain.intercept(event, eventType); // continue the chain
 *         System.out.println("After processing event: " + event.getClass().getSimpleName());
 *     }
 * }
 * }</pre>
 *
 * <p>When building an interceptor, it's important to decide whether to proceed with the chain.
 * If an interceptor chooses not to call {@link ApplicationEventInterceptorChain#intercept}, the event
 * processing will be short-circuited, and subsequent interceptors (as well as the final event handler)
 * will not be invoked.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationEventInterceptor
 * @since 1.0.0
 */
public interface ApplicationEventInterceptorChain {

    /**
     * Causes the next interceptor in the chain to be invoked, or if the calling interceptor is the last interceptor
     * in the chain, causes the resource at the end of the chain to be invoked.
     *
     * @param event     {@link ApplicationEvent}
     * @param eventType {@link ResolvableType} to present the type of {@link ApplicationEvent}
     */
    void intercept(ApplicationEvent event, ResolvableType eventType);
}
