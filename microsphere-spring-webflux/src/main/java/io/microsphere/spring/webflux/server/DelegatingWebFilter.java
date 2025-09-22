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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;

/**
 * {@link WebFilter} class delegates the {@link WebFilter} beans
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebFilter
 * @since 1.0.0
 */
public class DelegatingWebFilter implements WebFilter, ApplicationListener<ContextRefreshedEvent> {

    private final CompositeWebFilter delegate = new CompositeWebFilter();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return delegate.filter(exchange, chain);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        initWebFilters(context);
    }

    private void initWebFilters(ApplicationContext context) {
        List<WebFilter> webFilters = getSortedBeans(context, WebFilter.class);
        for (WebFilter webFilter : webFilters) {
            if (webFilter != this) {
                delegate.addFilter(webFilter);
            }
        }
    }
}
