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
package io.microsphere.spring.cache.redis;


import org.springframework.context.annotation.Bean;

import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.ClassLoaderUtils.isPresent;

/**
 * Redis Configuration with TTL features
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see org.springframework.data.redis.cache.RedisCacheManager
 * @since 1.0.0
 */
public class TTLRedisConfiguration {

    private static final String REDIS_CACHE_MANAGER_CLASS_NAME = "org.springframework.data.redis.cache.RedisCacheManager";

    private static final boolean REDIS_CACHE_MANAGER_CLASS_PRESENT = isPresent(REDIS_CACHE_MANAGER_CLASS_NAME, getDefaultClassLoader());


}
