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
package io.microsphere.spring.redis.replicator.event;

import io.microsphere.spring.redis.event.RedisCommandEvent;
import org.springframework.context.ApplicationEvent;

/**
 * Redis Command Replicated Event
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisCommandEvent
 * @since 1.0.0
 */
public class RedisCommandReplicatedEvent extends ApplicationEvent {

    /**
     * Business domain (non-serialized field, initialized by the consumer)
     */
    private transient final String domain;

    public RedisCommandReplicatedEvent(RedisCommandEvent sourceEvent, String domain) {
        super(sourceEvent);
        this.domain = domain;
    }

    /**
     * @return Business domain (non-serialized field, initialized by the consumer)
     */
    public String getDomain() {
        return domain;
    }

    public RedisCommandEvent getSourceEvent() {
        return (RedisCommandEvent) getSource();
    }
}
