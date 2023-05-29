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

import io.microsphere.spring.test.redis.EnableRedisTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Abstract Redis Replicator Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@EnableRedisTest
@EmbeddedKafka(
        ports = 9092,
        topics = "redis-replicator-event-topic-default",
        brokerProperties = {
                "listeners = PLAINTEXT://127.0.0.1:9092",
                "auto.create.topics.enable = true"
        }
)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=127.0.0.1:9092"
})
@Disabled
public abstract class AbstractRedisReplicatorTest {

    @Autowired
    protected ConfigurableApplicationContext context;

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

}
