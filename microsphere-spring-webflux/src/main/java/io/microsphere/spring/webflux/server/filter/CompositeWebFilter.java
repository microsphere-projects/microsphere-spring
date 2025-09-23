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

import io.microsphere.annotation.Nonnull;
import org.slf4j.Logger;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Composite {@link WebFilter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebFilter
 * @since 1.0.0
 */
public class CompositeWebFilter implements WebFilter {

    protected final Logger logger = getLogger(getClass());

    private final List<WebFilter> webFilters;

    public CompositeWebFilter() {
        this(emptyList());
    }

    public CompositeWebFilter(List<WebFilter> webFilters) {
        this.webFilters = new LinkedList<>(webFilters);
    }

    /**
     * Add one {@link WebFilter}
     *
     * @param webFilter one {@link WebFilter}
     * @return {@code true} if the {@link WebFilter} was added successfully
     */
    public boolean addFilter(WebFilter webFilter) {
        if (webFilter == null) {
            logger.warn("The WebFilter must not be null");
            return false;
        }
        if (webFilter == this) {
            logger.warn("The WebFilter must not be itself");
            return false;
        }
        if (webFilters.contains(webFilter)) {
            logger.warn("The WebFilter was already added : {} ", webFilter);
            return false;
        }
        webFilters.add(webFilter);
        logger.trace("The WebFilter was added : {} ", webFilter);
        return true;
    }

    /**
     * Add one or more {@link WebFilter WebFilters}
     *
     * @param one    one {@link WebFilter}
     * @param others more {@link WebFilter WebFilters}
     * @return {@link CompositeWebFilter} self
     */
    @Nonnull
    public CompositeWebFilter addFilters(WebFilter one, WebFilter... others) {
        addFilter(one);
        for (WebFilter other : others) {
            addFilter(other);
        }
        return this;
    }

    /**
     * Get all {@link WebFilter WebFilters}
     *
     * @return all {@link WebFilter WebFilters}
     */
    @Nonnull
    public List<WebFilter> getWebFilters() {
        return unmodifiableList(webFilters);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Mono<Void> result = null;
        for (WebFilter webFilter : webFilters) {
            result = webFilter.filter(exchange, chain);
        }
        return result == null ? chain.filter(exchange) : result;
    }
}
