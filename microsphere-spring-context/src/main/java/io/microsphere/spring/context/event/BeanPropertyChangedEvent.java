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

import java.util.Objects;
import java.util.StringJoiner;

/**
 * The event published when a Beans'  is changed.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
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

    public Object getBean() {
        return getSource();
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

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
