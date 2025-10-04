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

import org.springframework.context.ApplicationEventPublisher;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * An adapter class that bridges JavaBeans {@link PropertyChangeListener} with Spring's event publishing mechanism.
 * This class converts a JavaBeans {@link PropertyChangeEvent} into a Spring application event
 * ({@link BeanPropertyChangedEvent}) and publishes it using the provided {@link ApplicationEventPublisher}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * ConfigurableApplicationContext context = ...; // typically provided by Spring
 * JavaBeansPropertyChangeListenerAdapter listenerAdapter = new JavaBeansPropertyChangeListenerAdapter(context);
 *
 * User user = new User();
 *
 * context.addApplicationListener((ApplicationListener<BeanPropertyChangedEvent>) event -> {
 * user.setName((String) event.getNewValue());
 * });
 *
 * assertNull(user.getName());
 *
 * PropertyChangeSupport support = new PropertyChangeSupport(user);
 * support.addPropertyChangeListener(listenerAdapter);
 *
 * String userName = "my-name";
 *
 * support.firePropertyChange("name", user.getName(), userName);
 *
 * assertEquals(userName, user.getName());
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class JavaBeansPropertyChangeListenerAdapter implements PropertyChangeListener {

    private final ApplicationEventPublisher applicationEventPublisher;

    public JavaBeansPropertyChangeListenerAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        BeanPropertyChangedEvent adaptedEvent = adapt(event);
        applicationEventPublisher.publishEvent(adaptedEvent);
    }

    private BeanPropertyChangedEvent adapt(PropertyChangeEvent event) {
        return new BeanPropertyChangedEvent(event.getSource(), event.getPropertyName(), event.getOldValue(), event.getNewValue());
    }
}
