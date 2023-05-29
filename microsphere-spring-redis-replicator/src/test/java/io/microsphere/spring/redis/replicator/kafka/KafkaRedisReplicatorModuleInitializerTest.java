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
package io.microsphere.spring.redis.replicator.kafka;

import io.microsphere.spring.redis.context.RedisInitializer;
import io.microsphere.spring.redis.event.RedisCommandEvent;
import io.microsphere.spring.redis.replicator.AbstractRedisReplicatorTest;
import io.microsphere.spring.redis.replicator.event.RedisCommandReplicatedEvent;
import io.microsphere.spring.redis.replicator.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * {@link KafkaRedisReplicatorModuleInitializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see KafkaRedisReplicatorModuleInitializer
 * @since 1.0.0
 */
@ContextConfiguration(
        classes = {
                KafkaConsumerRedisReplicatorConfiguration.class,
                KafkaRedisReplicatorModuleInitializerTest.class
        },
        initializers = RedisInitializer.class
)
@TestPropertySource(properties = {
        "microsphere.redis.enabled=true",
        "microsphere.redis.wrapped-redis-templates=stringRedisTemplate",
})
public class KafkaRedisReplicatorModuleInitializerTest extends AbstractRedisReplicatorTest {

    @Test
    public void test() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        Map<Object, Object> data = new HashMap<>();

        context.addApplicationListener((ApplicationListener<RedisCommandReplicatedEvent>) event -> {
            RedisCommandEvent redisCommandEvent = event.getSourceEvent();

            RedisSerializer keySerializer = stringRedisTemplate.getKeySerializer();
            RedisSerializer valueSerializer = stringRedisTemplate.getValueSerializer();
            Object key = keySerializer.deserialize((byte[]) redisCommandEvent.getArg(0));
            Object value = valueSerializer.deserialize((byte[]) redisCommandEvent.getArg(1));
            data.put(key, value);

            assertEquals("org.springframework.data.redis.connection.RedisStringCommands", redisCommandEvent.getInterfaceName());
            assertEquals("set", redisCommandEvent.getMethodName());
            assertArrayEquals(new String[]{"[B", "[B"}, redisCommandEvent.getParameterTypes());
            assertEquals("default", redisCommandEvent.getApplicationName());
            latch.countDown();
        });


        stringRedisTemplate.opsForValue().set("Key-1", "Value-1");
        latch.await();

        assertEquals("Value-1", data.get("Key-1"));
    }
}
