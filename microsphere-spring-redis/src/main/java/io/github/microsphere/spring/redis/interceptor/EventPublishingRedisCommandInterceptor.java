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
package io.github.microsphere.spring.redis.interceptor;

import io.github.microsphere.spring.redis.context.RedisContext;
import io.github.microsphere.spring.redis.event.RedisCommandEvent;
import io.github.microsphere.spring.redis.event.RedisConfigurationPropertyChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.connection.RedisCommands;

import static io.github.microsphere.spring.redis.util.RedisConstants.COMMAND_EVENT_EXPOSED_PROPERTY_NAME;

/**
 * {@link RedisCommandInterceptor} publishes {@link RedisCommandEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class EventPublishingRedisCommandInterceptor implements RedisCommandInterceptor, ApplicationListener<RedisConfigurationPropertyChangedEvent>, ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(EventPublishingRedisCommandInterceptor.class);

    public static final String BEAN_NAME = "eventPublishingRedisCommendInterceptor";

    private final RedisContext redisContext;

    private final String applicationName;

    private ApplicationEventPublisher applicationEventPublisher;

    private volatile boolean enabled = false;

    public EventPublishingRedisCommandInterceptor(RedisContext redisContext) {
        this.redisContext = redisContext;
        this.applicationName = redisContext.getApplicationName();
        setEnabled();
    }

    public void setEnabled() {
        this.enabled = redisContext.isCommandEventExposed();
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void afterExecute(RedisMethodContext<RedisCommands> context, Object result, Throwable failure) throws Throwable {
        if (isEnabled() && failure == null) {
            if (context.isWriteMethod(true)) { // The current method is a Redis write command
                // Publish Redis Command Event
                publishRedisCommandEvent(context);
            }
        }
    }

    private void publishRedisCommandEvent(RedisMethodContext<RedisCommands> context) {
        RedisCommandEvent redisCommandEvent = createRedisCommandEvent(context);
        if (redisCommandEvent != null) {
            // Event handling allows exceptions to be thrown
            applicationEventPublisher.publishEvent(redisCommandEvent);
        }
    }

    private RedisCommandEvent createRedisCommandEvent(RedisMethodContext<RedisCommands> redisMethodContext) {
        RedisCommandEvent redisCommandEvent = new RedisCommandEvent(redisMethodContext);
        return redisCommandEvent;
    }

    @Override
    public void onApplicationEvent(RedisConfigurationPropertyChangedEvent event) {
        if (event.hasProperty(COMMAND_EVENT_EXPOSED_PROPERTY_NAME)) {
            this.setEnabled();
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
