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

package io.microsphere.spring.webflux.server.filter;


import io.microsphere.spring.webflux.context.event.ServerRequestHandledEvent;
import io.microsphere.spring.webflux.test.AbstractWebFluxTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.WebHandler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link PublishingRequestHandledEventWebFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PublishingRequestHandledEventWebFilter
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        PublishingRequestHandledEventWebFilter.class,
        PublishingRequestHandledEventWebFilterTest.class,
})
class PublishingRequestHandledEventWebFilterTest extends AbstractWebFluxTest implements ApplicationListener<ServerRequestHandledEvent> {

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private WebHandler webHandler;

    @Test
    void testFilter() {
        testHelloWorld();
    }

    @Override
    public void onApplicationEvent(ServerRequestHandledEvent event) {
        assertSame(this.webHandler, event.getWebHandler());
        assertSame(event.getSource(), event.getWebHandler());
        assertNotNull(event.getExchange());
        assertNotNull(event.getSessionId());
        assertNull(event.getUserName());
    }
}