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

package io.microsphere.spring.webflux.server;


import io.microsphere.spring.webflux.context.request.ServerWebRequest;
import io.microsphere.spring.webflux.test.AbstractWebFluxTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static java.util.Locale.ENGLISH;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.context.i18n.LocaleContextHolder.getLocaleContext;
import static org.springframework.web.context.request.RequestContextHolder.getRequestAttributes;

/**
 * {@link RequestContextWebFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestContextWebFilter
 * @since 1.0.0
 */
@ContextConfiguration(
        classes = {
                RequestContextWebFilter.class,
                RequestContextWebFilterTest.class
        }
)
class RequestContextWebFilterTest extends AbstractWebFluxTest implements WebFilter {

    @Autowired
    private RequestContextWebFilter webFilter;

    @BeforeEach
    void setUp() {
        this.webFilter.setThreadContextInheritable(true);
    }

    @Test
    void testFilter() {
        testHelloWorld();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        RequestAttributes requestAttributes = getRequestAttributes();
        assertNotNull(requestAttributes);
        assertTrue(requestAttributes instanceof ServerWebRequest);

        ServerWebRequest serverWebRequest = (ServerWebRequest) requestAttributes;
        assertSame(exchange, serverWebRequest.getExchange());

        LocaleContext localeContext = getLocaleContext();
        assertSame(ENGLISH, localeContext.getLocale());
        return chain.filter(exchange);
    }
}