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

import java.util.StringJoiner;

/**
 * An event that is published when a property of a bean changes.
 * <p>
 * This event provides details about the bean whose property changed, the name of the property,
 * and the old and new values of the property. It can be used to track changes in bean properties
 * during runtime.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>
 * // Create and publish the event when a bean's property changes
 * BeanPropertyChangedEvent event = new BeanPropertyChangedEvent(myBean, "status", oldStatus, newStatus);
 * applicationEventPublisher.publishEvent(event);
 * </pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationEvent
 * @since 1.0.0
 */
public class BeanPropertyChangedEvent extends ApplicationEvent {

    private final String propertyName;

    private final Object oldValue;

    private final Object newValue;

    /**
     * The constructor
     *
     * @param bean         the source bean
     * @param propertyName the property name was changed
     * @param oldValue     the property value before changed
     * @param newValue     the property value after changed
     */
    public BeanPropertyChangedEvent(Object bean, String propertyName, Object oldValue, Object newValue) {
        super(bean);
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Returns the bean whose property was changed.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   BeanPropertyChangedEvent event = new BeanPropertyChangedEvent(myBean, "event", null, newValue);
     *   Object bean = event.getBean();
     *   assertSame(myBean, bean);
     * }</pre>
     *
     * @return the source bean whose property changed
     */
    public Object getBean() {
        return getSource();
    }

    /**
     * Returns the name of the property that was changed.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   BeanPropertyChangedEvent event = new BeanPropertyChangedEvent(myBean, "event", null, newValue);
     *   assertEquals("event", event.getPropertyName());
     * }</pre>
     *
     * @return the name of the changed property
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Returns the value of the property before the change.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   BeanPropertyChangedEvent event = new BeanPropertyChangedEvent(myBean, "event", null, newValue);
     *   assertNull(event.getOldValue());
     * }</pre>
     *
     * @return the old property value, may be {@code null}
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Returns the value of the property after the change.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   BeanPropertyChangedEvent event = new BeanPropertyChangedEvent(myBean, "event", null, newValue);
     *   assertSame(newValue, event.getNewValue());
     * }</pre>
     *
     * @return the new property value, may be {@code null}
     */
    public Object getNewValue() {
        return newValue;
    }

    /**
     * Returns a string representation of this event, including the source bean,
     * property name, old value, and new value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   BeanPropertyChangedEvent event = new BeanPropertyChangedEvent(myBean, "event", null, newValue);
     *   String str = event.toString();
     *   assertNotNull(str);
     * }</pre>
     *
     * @return a string representation of this event
     */
    @Override
    public String toString() {
        return new StringJoiner(", ", BeanPropertyChangedEvent.class.getSimpleName() + "[", "]")
                .add("source=" + source)
                .add("propertyName='" + propertyName + "'")
                .add("oldValue=" + oldValue)
                .add("newValue=" + newValue)
                .toString();
    }
}
