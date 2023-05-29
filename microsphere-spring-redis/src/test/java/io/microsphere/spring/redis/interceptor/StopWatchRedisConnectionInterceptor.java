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
package io.microsphere.spring.redis.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;

/**
 * Logging
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class StopWatchRedisConnectionInterceptor implements RedisConnectionInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(StopWatchRedisConnectionInterceptor.class);

    @Override
    public void beforeExecute(RedisMethodContext context) {
        context.start();
    }


    @Override
    public void afterExecute(RedisMethodContext<RedisConnection> context, Object result, Throwable failure) throws Throwable {
        context.stop();
        logger.info("{} , result : {} , failure : {}", context, result, failure);
    }
}
