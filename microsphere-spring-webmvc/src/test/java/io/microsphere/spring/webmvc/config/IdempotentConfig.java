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
package io.microsphere.spring.webmvc.config;

import io.microsphere.logging.Logger;
import io.microsphere.spring.web.event.HandlerMethodArgumentsResolvedEvent;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.webmvc.annotation.Idempotent;
import org.springframework.context.event.EventListener;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * {@link Idempotent} Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Idempotent
 * @since 1.0.0
 */
public class IdempotentConfig {

    private static final Logger logger = getLogger(IdempotentConfig.class);

    @EventListener(WebEndpointMappingsReadyEvent.class)
    public void onEvent(WebEndpointMappingsReadyEvent event) {
        logger.info(event.toString());
    }

    @EventListener(HandlerMethodArgumentsResolvedEvent.class)
    public void onEvent(HandlerMethodArgumentsResolvedEvent event) {
        logger.info(event.toString());
    }
}
