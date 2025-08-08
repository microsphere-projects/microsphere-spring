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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static io.microsphere.spring.context.event.DeferredApplicationEventPublisher.PUBLISH_EVENT_METHOD;
import static io.microsphere.spring.context.event.DeferredApplicationEventPublisher.detectPublishEventMethod;
import static org.junit.Assert.assertEquals;

/**
 * {@link DeferredApplicationEventPublisher} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class DeferredApplicationEventPublisherTest {

    private AnnotationConfigApplicationContext context;

    @Before
    public void setUp() {
        this.context = new AnnotationConfigApplicationContext();
    }

    @After
    public void tearDown() {
        this.context.close();
        // reset
        PUBLISH_EVENT_METHOD = detectPublishEventMethod();
    }

    @Test
    public void testOnNoDefer() {
        testOn(false);
    }

    @Test
    public void testOnDefer() {
        testOn(true);
    }

    void testOn(boolean shouldDefer) {
        if (shouldDefer) {
            PUBLISH_EVENT_METHOD = null;
        } else {
            PUBLISH_EVENT_METHOD = detectPublishEventMethod();
        }
        this.context.register(TestComponent.class);
        this.context.refresh();
        TestComponent testComponent = this.context.getBean(TestComponent.class);
        assertEquals("Hello,World", testComponent.getLatestTestEvent().getSource());
        testComponent.publishEvent("Testing");
    }

}

class TestEvent extends ApplicationEvent {

    public TestEvent(String source) {
        super(source);
    }
}

class TestComponent implements BeanFactoryPostProcessor, ApplicationEventPublisherAware, ApplicationListener<TestEvent> {

    private final boolean shouldDefer;

    private TestEvent latestTestEvent;

    private ApplicationEventPublisher applicationEventPublisher;

    TestComponent() {
        this.shouldDefer = PUBLISH_EVENT_METHOD == null;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = shouldDefer ? new DeferredApplicationEventPublisher(applicationEventPublisher, true)
                : new DeferredApplicationEventPublisher(applicationEventPublisher);
    }

    @Override
    public void onApplicationEvent(TestEvent event) {
        latestTestEvent = event;
    }

    public TestEvent getLatestTestEvent() {
        return latestTestEvent;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.applicationEventPublisher.publishEvent(new TestEvent("Hello,World"));
    }

    public void publishEvent(Object event) {
        this.applicationEventPublisher.publishEvent(event);
    }
}