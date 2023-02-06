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
package io.github.microsphere.spring.redis.util;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * The constants of Redis
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface RedisConstants {

    /**
     * {@link RedisTemplate} Source type
     */
    byte REDIS_TEMPLATE_SOURCE_TYPE = 1;

    /**
     * {@link RedisConnectionFactory} source type
     */
    byte REDIS_CONNECTION_FACTORY_SOURCE_TYPE = 2;

    /**
     * The other source type
     */
    byte OTHER_SOURCE_TYPE = -1;

    /**
     * {@link RedisTemplate} Bean Name
     */
    String REDIS_TEMPLATE_BEAN_NAME = "redisTemplate";

    /**
     * {@link StringRedisTemplate} Bean Name
     */
    String STRING_REDIS_TEMPLATE_BEAN_NAME = "stringRedisTemplate";

    String PROPERTY_NAME_PREFIX = "microsphere.redis.";

    String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "enabled";

    boolean DEFAULT_ENABLED = Boolean.getBoolean(ENABLED_PROPERTY_NAME);

    String COMMAND_EVENT_PROPERTY_NAME_PREFIX = PROPERTY_NAME_PREFIX + "command-event.";

    String COMMAND_EVENT_EXPOSED_PROPERTY_NAME = COMMAND_EVENT_PROPERTY_NAME_PREFIX + "exposed";

    boolean DEFAULT_COMMAND_EVENT_EXPOSED = true;

    String FAIL_FAST_ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "fail-fast";

    boolean FAIL_FAST_ENABLED = Boolean.getBoolean(System.getProperty(FAIL_FAST_ENABLED_PROPERTY_NAME, "true"));

    /**
     * Wrapped {@link RedisTemplate} list of Bean names
     */
    String WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "wrapped-redis-templates";

    /**
     * The all wrapped bean names of {@link RedisTemplate}: "*"
     */
    List<String> ALL_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES = unmodifiableList(asList("*"));

    /**
     * The prefix of Redis Interceptors' property name
     */
    String INTERCEPTOR_PROPERTY_NAME_PREFIX = PROPERTY_NAME_PREFIX + "interceptor.";

    String INTERCEPTOR_ENABLED_PROPERTY_NAME = INTERCEPTOR_PROPERTY_NAME_PREFIX + "enabled";

    boolean DEFAULT_INTERCEPTOR_ENABLED = true;

    String DEFAULT_WRAP_REDIS_TEMPLATE_PLACEHOLDER = "${" + WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME + ":}";

}
