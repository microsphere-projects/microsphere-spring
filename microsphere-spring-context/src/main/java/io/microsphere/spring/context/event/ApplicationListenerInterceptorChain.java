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
import org.springframework.context.ApplicationListener;

/**
 * A contract for an interceptor chain that allows the execution flow to be intercepted and processed
 * by a series of {@link ApplicationListenerInterceptor} instances. This chain enables pre-processing,
 * post-processing, and around-processing of events before and after they are handled by the actual
 * {@link ApplicationListener}.
 *
 * <p>
 * Implementations of this interface are responsible for managing the order and execution of interceptors.
 * Each interceptor in the chain has the ability to decide whether to pass the invocation to the next
 * interceptor or to short-circuit the chain.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class CustomInterceptor implements ApplicationListenerInterceptor {
 *     public void intercept(ApplicationListenerInterceptorChain chain, ApplicationListener<?> listener, ApplicationEvent event) {
 *         // Pre-processing logic
 *         System.out.println("Before event handling");
 *
 *         // Proceed to the next interceptor or the target listener
 *         chain.intercept(listener, event);
 *
 *         // Post-processing logic
 *         System.out.println("After event handling");
 *     }
 * }
 * }</pre>
 *
 * <p>
 * In the example above, the custom interceptor adds behavior before and after the event is processed
 * by the target application listener. The call to {@link #intercept(ApplicationListener, ApplicationEvent)}
 * ensures the chain continues execution.
 * </p>
 *
 * @see ApplicationListenerInterceptor
 * @see ApplicationListener
 * @see ApplicationEvent
 */
public interface ApplicationListenerInterceptorChain {

    /**
     * Invokes the next interceptor in the chain, or if the current interceptor is the last one,
     * dispatches the event to the target {@link ApplicationListener}.
     *
     * <p>This method is typically called after any pre-processing logic and before post-processing logic,
     * allowing the chain to proceed with its execution. If used in an interceptor's
     * {@link ApplicationListenerInterceptor#intercept(ApplicationListener, ApplicationEvent, ApplicationListenerInterceptorChain)}
     * method, it ensures that the event continues to be processed by subsequent interceptors or the final
     * application listener.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class LoggingInterceptor implements ApplicationListenerInterceptor {
     *     public void intercept(ApplicationListenerInterceptorChain chain, ApplicationListener<?> listener, ApplicationEvent event) {
     *         System.out.println("Before event reaches the listener");
     *
     *         // Proceed with the chain
     *         chain.intercept(listener, event);
     *
     *         System.out.println("After event has been handled by the listener");
     *     }
     * }
     * }</pre>
     *
     * <p>In this example, the interceptor logs a message before and after the event is processed by the
     * actual listener or the next interceptor in the chain.</p>
     *
     * @param applicationListener the target {@link ApplicationListener} that will handle the event
     * @param event               the {@link ApplicationEvent} to be processed by the chain or the listener
     */
    void intercept(ApplicationListener<?> applicationListener, ApplicationEvent event);
}
