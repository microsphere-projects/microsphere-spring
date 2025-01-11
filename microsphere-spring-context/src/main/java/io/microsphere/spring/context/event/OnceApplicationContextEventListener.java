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
 * The abstract class {@link ApplicationListener} for {@link ApplicationEvent} guarantees just one-time execution
 * and prevents the event propagation in the hierarchical {@link ApplicationContext ApplicationContexts}
 *
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
