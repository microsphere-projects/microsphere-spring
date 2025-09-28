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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.filter.reactive.HiddenHttpMethodFilter;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.handler.DefaultWebFilterChain;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.webflux.test.WebTestUtils.mockServerWebExchange;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CompositeWebFilter
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see {@link CompositeWebFilter}
 * @since 1.0.0
 */
class CompositeWebFilterTest {

    private CompositeWebFilter webFilter;

    @BeforeEach
    void setUp() {
        this.webFilter = new CompositeWebFilter();
    }

    @Test
    void testAddFilter() {
        assertFalse(this.webFilter.addFilter(null));
        assertFalse(this.webFilter.addFilter(this.webFilter));

        HiddenHttpMethodFilter hiddenHttpMethodFilter = new HiddenHttpMethodFilter();
        assertTrue(this.webFilter.addFilter(hiddenHttpMethodFilter));
        assertFalse(this.webFilter.addFilter(hiddenHttpMethodFilter));
    }

    @Test
    void testAddFilters() {
        assertSame(this.webFilter, this.webFilter.addFilters(null, this.webFilter, new HiddenHttpMethodFilter()));
    }

    @Test
    void testRemoveFilter() {
        assertFalse(this.webFilter.removeFilter(null));
        assertFalse(this.webFilter.removeFilter(this.webFilter));
        testAddFilter();
        for (WebFilter filter : this.webFilter.getWebFilters()) {
            assertTrue(this.webFilter.removeFilter(filter));
        }
    }

    @Test
    void testFilterWithFilter() {
        testAddFilter();
        assertFilter(this.webFilter);
    }

    @Test
    void testFilterWithoutFilter() {
        assertFilter(this.webFilter);
    }

    static void assertFilter(CompositeWebFilter webFilter) {
        assertFilter(webFilter, webFilter.getWebFilters().toArray(new WebFilter[0]));
    }

    static void assertFilter(WebFilter containerFilter, WebFilter... elementFilters) {
        ServerWebExchange exchange = mockServerWebExchange();
        WebHandler webHandler = new DispatcherHandler();
        WebFilterChain chain = new DefaultWebFilterChain(webHandler, ofList(elementFilters));
        assertNotNull(containerFilter.filter(exchange, chain));
    }
}