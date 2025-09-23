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

package io.microsphere.spring.webflux.util;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.security.Principal;

import static io.microsphere.lang.function.ThrowableSupplier.execute;

/**
 * The utils class for Spring Web
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServerWebExchange
 * @since 1.0.0
 */
public abstract class WebUtils {

    public static WebSession getSession(ServerWebExchange exchange) {
        Mono<WebSession> session = exchange.getSession();
        return getValue(session);
    }

    public static Principal getPrincipal(ServerWebExchange exchange) {
        Mono<Principal> principal = exchange.getPrincipal();
        return getValue(principal);
    }

    public static String getSessionId(ServerWebExchange exchange) {
        WebSession webSession = getSession(exchange);
        String sessionId = webSession.getId();
        return sessionId;
    }

    public static String getUserName(ServerWebExchange exchange) {
        Principal principal = getPrincipal(exchange);
        return principal == null ? null : principal.getName();
    }

    static <T> T getValue(Mono<T> mono) {
        return execute(() -> mono.toFuture().get());
    }

    private WebUtils() {
    }
}
