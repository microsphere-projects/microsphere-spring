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

import io.microsphere.spring.util.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.beans.PropertyChangeSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link JavaBeansPropertyChangeListenerAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {JavaBeansPropertyChangeListenerAdapterTest.class})
public class JavaBeansPropertyChangeListenerAdapterTest {

    @Autowired
    private ConfigurableApplicationContext context;

    @Test
    public void testPropertyChange() {
        JavaBeansPropertyChangeListenerAdapter listenerAdapter = new JavaBeansPropertyChangeListenerAdapter(context);

        User user = new User();

        context.addApplicationListener((ApplicationListener<BeanPropertyChangedEvent>) event -> {
            user.setName((String) event.getNewValue());
        });

        assertNull(user.getName());

        PropertyChangeSupport support = new PropertyChangeSupport(user);
        support.addPropertyChangeListener(listenerAdapter);

        String userName = "my-name";

        support.firePropertyChange("name", user.getName(), userName);

        assertEquals(userName, user.getName());
    }

}
