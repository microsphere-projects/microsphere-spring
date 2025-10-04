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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.microsphere.spring.context.ApplicationContextUtils.asConfigurableApplicationContext;
import static io.microsphere.util.Assert.assertNotNull;
import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

/**
 * Before Spring Framework 4.2, {@link AbstractApplicationContext} is an implementation of {@link ApplicationEventPublisher}
 * can't handle the early {@link ApplicationEvent event} that is {@link #publishEvent(ApplicationEvent) published}
 * before {@link ApplicationEventMulticaster}'s initialization, in this scenario, {@link DeferredApplicationEventPublisher}
 * is introduced and used to resolve {@link #publishEvent(ApplicationEvent)} too early
 * to publish {@link ApplicationEvent} when {@link AbstractApplicationContext#initApplicationEventMulticaster()
 * Spring ApplicationContexts' ApplicationEventMulticaster} is not ready.
 * First, {@link DeferredApplicationEventPublisher} stores these early events temporarily, and then
 * {@link #replayDeferredEvents() re-publish} them on {@link ContextRefreshedEvent Application context is ready}.
 * <p>
 * In contrast, If current runtime is based on Spring Framework that {@link #supportsEarlyApplicationEvents() supports
 * early application events}, {@link DeferredApplicationEventPublisher} only delegates the
 * {@link ConfigurableApplicationContext Application Context} that was injected by
 * {@link #DeferredApplicationEventPublisher(ApplicationEventPublisher) constructor}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Deprecated
public class DeferredApplicationEventPublisher implements ApplicationEventPublisher, ApplicationListener<ContextRefreshedEvent> {

    /**
     * The method name of publishEvent(Object)
     */
    static final String PUBLISH_EVENT_OBJECT_METHOD_NAME = "publishEvent";

    /**
     * {@link ApplicationEventPublisher#publishEvent(Object)} method
     */
    static Method PUBLISH_EVENT_METHOD = detectPublishEventMethod();

    private final ApplicationEventPublisher delegate;

    /**
     * May be null if the argument of {@link ApplicationEventPublisher} from constructor
     */
    private final ConfigurableApplicationContext context;

    private final ConcurrentLinkedQueue<ApplicationEvent> deferredEvents = new ConcurrentLinkedQueue<>();

    private final boolean shouldDefer;

    /**
     * @param delegate {@link ApplicationEventPublisher}
     */
    public DeferredApplicationEventPublisher(ApplicationEventPublisher delegate) {
        this(delegate, !supportsPublishEventMethod());
    }

    protected DeferredApplicationEventPublisher(ApplicationEventPublisher delegate, boolean shouldDefer) {
        assertNotNull(delegate, () -> "The ApplicationEventPublisher argument must not be null");
        this.delegate = delegate;
        this.context = asConfigurableApplicationContext(delegate);
        if (this.context != null) {
            this.context.addApplicationListener(this);
        }
        this.shouldDefer = shouldDefer;
    }

    @Override
    public void publishEvent(ApplicationEvent event) {
        if (shouldDefer) {
            // before Spring 4.2
            deferEvent(event);
        } else {
            doPublishEvent(event);
        }
    }

    void doPublishEvent(ApplicationEvent event) {
        delegate.publishEvent(event);
    }

    void deferEvent(ApplicationEvent event) {
        deferredEvents.offer(event);
    }

    /**
     * Current method will not be invoked before Spring 4.2
     *
     * @param event the {@link ApplicationEvent} or the payload of {@link ApplicationEvent event}
     */
    public void publishEvent(Object event) {
        if (supportsPublishEventMethod()) {
            // invoke by reflection to resolve the compilation issue
            invokeMethod(PUBLISH_EVENT_METHOD, delegate, event);
        } else { // before Spring 4.2
            // DO NOTHING, just resolve the compilation issue in Spring 4.2 and above
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (shouldDefer) {
            ApplicationContext currentContext = event.getApplicationContext();
            if (!currentContext.equals(delegate)) {
                // prevent multiple event multi-casts in hierarchical contexts
                return;
            }
            replayDeferredEvents();
        }
    }

    private void replayDeferredEvents() {
        Iterator<ApplicationEvent> iterator = deferredEvents.iterator();
        while (iterator.hasNext()) {
            ApplicationEvent event = iterator.next();
            doPublishEvent(event);
            iterator.remove(); // remove if published
        }
    }

    static boolean supportsPublishEventMethod() {
        return PUBLISH_EVENT_METHOD != null;
    }

    static Method detectPublishEventMethod() {
        return findMethod(ApplicationEventPublisher.class, PUBLISH_EVENT_OBJECT_METHOD_NAME, Object.class);
    }
}
