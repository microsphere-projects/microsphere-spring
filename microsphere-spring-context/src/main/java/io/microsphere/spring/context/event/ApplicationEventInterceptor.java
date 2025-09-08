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
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;

/**
 * An interface used to intercept {@link ApplicationEvent application events} allowing for pre-processing,
 * post-processing, or even prevention of event propagation through the
 * {@link ApplicationEventInterceptorChain interceptor chain}.
 *
 * <p>Interceptors can be used to implement cross-cutting concerns such as logging, security checks,
 * or performance monitoring around the application event handling process.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class LoggingInterceptor implements ApplicationEventInterceptor {
 *     @Override
 *     public void intercept(ApplicationEvent event, ResolvableType eventType, ApplicationEventInterceptorChain chain) {
 *         System.out.println("Before handling event: " + event);
 *         chain.intercept(event, eventType); // Continue the interceptor chain
 *         System.out.println("After handling event: " + event);
 *     }
 * }
 * }</pre>
 *
 * <p>Interceptors are typically ordered using the {@link Ordered} interface or
 * {@link org.springframework.core.annotation.Order @Order} annotation to control the order in which they are applied.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationEvent
 * @see ApplicationEventInterceptorChain
 * @see Ordered
 * @since 1.0.0
 */
public interface ApplicationEventInterceptor extends Ordered {

    /**
     * Intercept the specified {@link ApplicationEvent} with its resolved type, allowing custom
     * pre-processing, post-processing, or short-circuiting the event propagation by not invoking
     * the next interceptor in the chain.
     *
     * <h3>Example Usage</h3>
     * Implementing a simple logging interceptor:
     * <pre>{@code
     * public class SampleInterceptor implements ApplicationEventInterceptor {
     *     public void intercept(ApplicationEvent event, ResolvableType eventType, ApplicationEventInterceptorChain chain) {
     *         System.out.println("Intercepting event: " + event);
     *         // Proceed to the next interceptor in the chain
     *         chain.intercept(event, eventType);
     *         System.out.println("Finished handling event: " + event);
     *     }
     * }
     * }</pre>
     *
     * <strong>Short-circuiting:</strong> To prevent further processing of an event, simply
     * skip calling {@link ApplicationEventInterceptorChain#intercept(ApplicationEvent, ResolvableType)}:
     * <pre>{@code
     * public class SilentEventInterceptor implements ApplicationEventInterceptor {
     *     public void intercept(ApplicationEvent event, ResolvableType eventType, ApplicationEventInterceptorChain chain) {
     *         // Skip calling chain.proceed() to prevent event propagation
     *         System.out.println("Event blocked: " + event);
     *     }
     * }
     * }</pre>
     *
     * @param event     the event being intercepted; never {@code null}
     * @param eventType the resolved type of the event, useful for filtering or conditional logic
     * @param chain     the interceptor chain to continue processing if desired
     */
    void intercept(ApplicationEvent event, ResolvableType eventType, ApplicationEventInterceptorChain chain);

    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
