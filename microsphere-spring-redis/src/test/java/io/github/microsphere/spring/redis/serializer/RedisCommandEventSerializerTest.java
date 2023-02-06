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
package io.github.microsphere.spring.redis.serializer;

import io.github.microsphere.spring.redis.event.RedisCommandEvent;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.Method;

import static io.github.microsphere.spring.redis.metadata.RedisMetadataRepository.findWriteCommandMethod;
import static io.github.microsphere.spring.redis.serializer.RedisCommandEventSerializer.VERSION_V1;


/**
 * {@link RedisCommandEventSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since
 */
public class RedisCommandEventSerializerTest extends AbstractSerializerTest<RedisCommandEvent> {

    @Override
    protected RedisSerializer<RedisCommandEvent> getSerializer() {
        return new RedisCommandEventSerializer();
    }

    @Override
    protected RedisCommandEvent getValue() {
        String interfaceName = "org.springframework.data.redis.connection.RedisStringCommands";
        String methodName = "set";
        String[] parameterTypes = new String[]{"[B", "[B"};
        Method method = findWriteCommandMethod(interfaceName, methodName, parameterTypes);
        String applicationName = "test";
        RedisCommandEvent.Builder builder = RedisCommandEvent.Builder.source("test")
                .applicationName(applicationName)
                .method(method)
                .args("A".getBytes(), "B".getBytes())
                .serializationVersion(VERSION_V1);
        return builder.build();
    }
}
