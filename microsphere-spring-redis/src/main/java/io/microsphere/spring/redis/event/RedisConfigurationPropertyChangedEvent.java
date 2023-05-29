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
package io.microsphere.spring.redis.event;

import io.microsphere.spring.redis.config.RedisConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.core.env.Environment;

import java.util.Set;

/**
 * Redis Configuration property changed event
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class RedisConfigurationPropertyChangedEvent extends ApplicationContextEvent {

    private final Environment environment;

    private final Set<String> propertyNames;

    public RedisConfigurationPropertyChangedEvent(ConfigurableApplicationContext source, Set<String> propertyNames) {
        super(source);
        this.environment = source.getEnvironment();
        this.propertyNames = propertyNames;
    }

    @Override
    public ConfigurableApplicationContext getSource() {
        return (ConfigurableApplicationContext) super.getSource();
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Set<String> getPropertyNames() {
        return propertyNames;
    }

    public boolean hasProperty(String propertyName) {
        return propertyNames.contains(propertyName);
    }

    public RedisConfiguration getRedisConfiguration() {
        return RedisConfiguration.get(getSource());
    }
}
