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
package io.github.microsphere.spring.beans.factory.annotation;

/**
 * {@link EnableConfigurationBeanBinding} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.3
 */

import io.github.microsphere.spring.context.config.ConfigurationBeanBinder;
import io.github.microsphere.spring.context.config.ConfigurationBeanCustomizer;
import io.github.microsphere.spring.context.config.DefaultConfigurationBeanBinder;
import io.github.microsphere.spring.util.User;
import org.junit.Test;
import org.springframework.context.annotation.Bean;

import static org.junit.Assert.assertEquals;


@EnableConfigurationBeanBinding(prefix = "usr", type = User.class)
public class EnableConfigurationBeanBindingTest extends AbstractEnableConfigurationBeanBindingTest {

    @Bean
    public ConfigurationBeanCustomizer customizer() {
        return new ConfigurationBeanCustomizer() {

            @Override
            public int getOrder() {
                return 0;
            }

            @Override
            public void customize(String beanName, Object configurationBean) {
                if ("m".equals(beanName) && configurationBean instanceof User) {
                    User user = (User) configurationBean;
                    user.setAge(19);
                }
            }
        };
    }

    @Bean
    public ConfigurationBeanBinder configurationBeanBinder() {
        return new DefaultConfigurationBeanBinder();
    }

    @Test
    public void testUser() {
        User user = context.getBean("m", User.class);
        assertEquals("mercyblitz", user.getName());
        assertEquals(19, user.getAge());
    }
}
