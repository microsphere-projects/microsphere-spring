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
import org.springframework.core.Ordered;

/**
 * An interceptor interface for {@link ApplicationListener} to provide additional behavior before or after
 * the listener processes an event. Interceptors can be used to perform cross-cutting concerns such as logging,
 * security checks, or performance monitoring.
 *
 * <p>Implementations should typically implement the {@link #intercept(ApplicationListener, ApplicationEvent, ApplicationListenerInterceptorChain)}
 * method to define their specific behavior. If multiple interceptors are registered, they will be ordered by their
 * respective order values, as defined in the {@link Ordered} interface.
 *
 * <h3>Example Usage</h3>
 *
 * <pre>{@code
 * public class LoggingApplicationListenerInterceptor implements ApplicationListenerInterceptor {
 *
 *     private static final Logger logger = LoggerFactory.getLogger(LoggingApplicationListenerInterceptor.class);
 *
 *     @Override
 *     public void intercept(ApplicationListener<?> applicationListener, ApplicationEvent event, ApplicationListenerInterceptorChain chain) {
 *         try {
 *             logger.info("Before handling event: {}", event.getClass().getSimpleName());
 *             chain.intercept(applicationListener, event);
 *         } finally {
 *             logger.info("After handling event: {}", event.getClass().getSimpleName());
 *         }
 *     }
 *
 *     @Override
 *     public int getOrder() {
 *         return -1000; // Set a custom order value
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationListener
 * @see ApplicationEvent
 * @see ApplicationListenerInterceptorChain
 * @since 1.0.0
 */
public interface ApplicationListenerInterceptor extends Ordered {

    /**
     * Intercept the specified {@link ApplicationListener} when it processes an {@link ApplicationEvent}.
     * Implementing classes can perform custom logic before or after delegating to the next element in the
     * {@link ApplicationListenerInterceptorChain}.
     *
     * <p><b>Before Processing:</b> Interceptors may perform setup operations, such as logging event details,
     * starting a timer for performance tracking, or applying contextual information.</p>
     *
     * <p><b>After Processing:</b> Interceptors may perform cleanup operations, such as stopping a timer,
     * logging completion status, or publishing metrics.</p>
     *
     * <p>The typical implementation will wrap the call to the chain like this:</p>
     *
     * <pre>{@code
     * void intercept(ApplicationListener<?> listener, ApplicationEvent event, ApplicationListenerInterceptorChain chain) {
     *     // Perform pre-processing logic here
     *
     *     try {
     *         // Proceed with the interceptor chain
     *         chain.intercept(listener, event);
     *     } finally {
     *         // Perform post-processing logic here
     *     }
     * }
     * }</pre>
     *
     * @param applicationListener the listener that is processing the event
     * @param event               the event being processed
     * @param chain               the interceptor chain used to continue processing
     */
    void intercept(ApplicationListener<?> applicationListener, ApplicationEvent event, ApplicationListenerInterceptorChain chain);

    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
