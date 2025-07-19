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

import io.microsphere.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static org.springframework.util.ClassUtils.getShortName;
import static org.springframework.util.ObjectUtils.nullSafeEquals;

/**
 * Abstract {@link ApplicationListener} base class for handling {@link ApplicationContextEvent} in a one-time execution manner.
 * <p>
 * This class ensures that the event is processed only once and prevents event propagation across hierarchical
 * {@link ApplicationContext} instances.
 * </p>
 *
 * <h3>Example Usage</h3>
 *
 * <pre>{@code
 * public class MyApplicationContextEventListener
 *     extends OnceApplicationContextEventListener<ContextRefreshedEvent> {
 *
 *     @Override
 *     protected void onApplicationContextEvent(ContextRefreshedEvent event) {
 *         // Handle the event only once in the original context
 *         System.out.println("Context refreshed: " + event.getApplicationContext());
 *     }
 * }
 * }</pre>
 *
 * <p>
 * This ensures that the event handler logic in {@link #onApplicationContextEvent(ApplicationContextEvent)} is executed
 * only if the event comes from the same {@link ApplicationContext} that was set during initialization.
 * </p>
 *
 * @param <E> the specific {@link ApplicationContextEvent} type to listen for
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ApplicationListener
 * @see ApplicationContextEvent
 * @since 1.0.0
 */
public abstract class OnceApplicationContextEventListener<E extends ApplicationContextEvent> implements ApplicationListener<E>,
        ApplicationContextAware {

    protected final Logger logger = getLogger(getClass());

    private ApplicationContext applicationContext;

    public OnceApplicationContextEventListener() {
    }

    public OnceApplicationContextEventListener(ApplicationContext applicationContext) {
        setApplicationContext(applicationContext);
    }

    public final void onApplicationEvent(E event) {
        if (isOriginalEventSource(event)) {
            onApplicationContextEvent(event);
        }
    }

    /**
     * The subclass overrides this method to handle {@link ApplicationContextEvent}
     *
     * @param event {@link ApplicationContextEvent}
     */
    protected abstract void onApplicationContextEvent(E event);

    /**
     * Is original {@link ApplicationContext} as the event source
     *
     * @param event {@link ApplicationEvent}
     * @return if original, return <code>true</code>, or <code>false</code>
     */
    private boolean isOriginalEventSource(ApplicationEvent event) {

        boolean originalEventSource = nullSafeEquals(getApplicationContext(), event.getSource());

        if (!originalEventSource) {
            if (logger.isTraceEnabled()) {
                logger.trace("The source of event[" + event.getSource() + "] is not original!");
            }
        }

        return originalEventSource;
    }

    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            throw new NullPointerException("applicationContext must be not null, it has to invoke " +
                    "setApplicationContext(ApplicationContext) method first if "
                    + getShortName(getClass()) + " instance is not a Spring Bean");
        }
        return applicationContext;
    }
}
