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

package io.microsphere.spring.webflux.context.event;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.spring.webflux.util.WebUtils;
import org.springframework.web.context.support.RequestHandledEvent;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;

/**
 * {@link RequestHandledEvent} class for Spring WebFlux
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestHandledEvent
 * @see org.springframework.web.context.support.ServletRequestHandledEvent
 * @since 1.0.0
 */
public class ServerRequestHandledEvent extends RequestHandledEvent {

    private final ServerWebExchange exchange;

    public ServerRequestHandledEvent(@Nonnull WebHandler webHandler, @Nonnull ServerWebExchange exchange,
                                     long processingTimeMillis) {
        this(webHandler, exchange, processingTimeMillis, null);
    }

    public ServerRequestHandledEvent(@Nonnull WebHandler webHandler, @Nonnull ServerWebExchange exchange,
                                     long processingTimeMillis, @Nullable Throwable failureCause) {
        super(webHandler, WebUtils.getSessionId(exchange), WebUtils.getUserName(exchange), processingTimeMillis, failureCause);
        this.exchange = exchange;
    }

    /**
     * Get the {@link WebHandler}
     *
     * @return the {@link WebHandler}
     */
    @Nonnull
    public WebHandler getWebHandler() {
        return (WebHandler) getSource();
    }

    /**
     * Get the {@link ServerWebExchange}
     *
     * @return the {@link ServerWebExchange}
     */
    @Nonnull
    public ServerWebExchange getExchange() {
        return exchange;
    }
}
