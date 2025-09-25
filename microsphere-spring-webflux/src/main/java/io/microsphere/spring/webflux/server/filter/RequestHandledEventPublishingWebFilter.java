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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.Ordered;
import org.springframework.web.context.support.RequestHandledEvent;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.handler.DefaultWebFilterChain;
import reactor.core.publisher.Mono;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * A WebFilter class to publish the {@link RequestHandledEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebFilter
 * @see ServerRequestHandledEvent
 * @see RequestHandledEvent
 * @since 1.0.0
 */
public class RequestHandledEventPublishingWebFilter implements WebFilter, ApplicationEventPublisherAware, Ordered {

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = nanoTime();
        return chain.filter(exchange).doOnTerminate(() -> {
            publishRequestHandledEvent(exchange, chain, startTime);
        });
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    private void publishRequestHandledEvent(ServerWebExchange exchange, WebFilterChain chain, long startTime) {
        RequestHandledEvent event = createRequestHandledEvent(exchange, chain, startTime);
        this.applicationEventPublisher.publishEvent(event);
    }

    private RequestHandledEvent createRequestHandledEvent(ServerWebExchange exchange, WebFilterChain chain, long startTime) {
        DefaultWebFilterChain filterChain = (DefaultWebFilterChain) chain;
        WebHandler webHandler = filterChain.getHandler();
        long processingTime = nanoTime() - startTime;
        long processingTimeMillis = NANOSECONDS.toMillis(processingTime);
        return new ServerRequestHandledEvent(webHandler, exchange, processingTimeMillis);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}