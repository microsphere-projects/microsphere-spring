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
package io.microsphere.spring.redis.replicator;

import io.microsphere.spring.redis.context.RedisInitializer;
import io.microsphere.spring.redis.replicator.event.RedisCommandReplicatedEvent;
import io.microsphere.spring.redis.replicator.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * {@link RedisCommandReplicator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisCommandReplicator
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        KafkaConsumerRedisReplicatorConfiguration.class,
        RedisCommandReplicator.class,
        RedisCommandReplicatorTest.class},
        initializers = RedisInitializer.class)
@TestPropertySource(properties = {
        "microsphere.redis.enabled=true"
})
public class RedisCommandReplicatorTest extends AbstractRedisReplicatorTest {

    @Test
    public void test() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        Map<Object, Object> data = new HashMap<>();

        context.addApplicationListener((ApplicationListener<RedisCommandReplicatedEvent>) event -> {
            latch.countDown();
        });


        stringRedisTemplate.opsForValue().set("Key-1", "Value-1");
        latch.await();
    }
}
