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

import io.microsphere.logging.Logger;
import io.microsphere.spring.webflux.context.request.ServerWebRequest;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

import static io.microsphere.collection.ListUtils.first;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static org.springframework.context.i18n.LocaleContextHolder.resetLocaleContext;
import static org.springframework.context.i18n.LocaleContextHolder.setLocale;
import static org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes;
import static org.springframework.web.context.request.RequestContextHolder.setRequestAttributes;

/**
 * The variant {@link RequestContextFilter} for Spring WebFlux
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestContextFilter
 * @since 1.0.0
 */
public class RequestContextWebFilter implements WebFilter, Ordered {

    protected final Logger logger = getLogger(getClass());

    private boolean threadContextInheritable = false;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerWebRequest requestAttributes = new ServerWebRequest(exchange);
        initContextHolders(requestAttributes);
        return chain.filter(exchange).doOnTerminate(() -> {
            resetContextHolders();
            logger.trace("Cleared thread-bound request context: " + exchange);
        });
    }

    /**
     * Set whether to expose the LocaleContext and RequestAttributes as inheritable
     * for child threads (using an {@link java.lang.InheritableThreadLocal}).
     * <p>Default is "false", to avoid side effects on spawned background threads.
     * Switch this to "true" to enable inheritance for custom child threads which
     * are spawned during request processing and only used for this request
     * (that is, ending after their initial task, without reuse of the thread).
     * <p><b>WARNING:</b> Do not use inheritance for child threads if you are
     * accessing a thread pool which is configured to potentially add new threads
     * on demand (for example, a JDK {@link java.util.concurrent.ThreadPoolExecutor}),
     * since this will expose the inherited context to such a pooled thread.
     */
    public void setThreadContextInheritable(boolean threadContextInheritable) {
        this.threadContextInheritable = threadContextInheritable;
    }

    /**
     * Get whether to expose the {@link LocaleContext} and {@link RequestAttributes} into {@link InheritableThreadLocal}.
     *
     * @return <code>true</code> if the {@link LocaleContext} and {@link RequestAttributes} are exposed into
     * {@link InheritableThreadLocal}, otherwise, <code>false</code>
     */
    public boolean isThreadContextInheritable() {
        return threadContextInheritable;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 1;
    }

    private void initContextHolders(ServerWebRequest requestAttributes) {
        Locale locale = getLocale(requestAttributes);
        setLocale(locale, this.threadContextInheritable);
        setRequestAttributes(requestAttributes, this.threadContextInheritable);
        logger.trace("Bound request context to thread[inheritable : {}]: {}", this.threadContextInheritable, requestAttributes);
    }

    protected Locale getLocale(ServerWebRequest requestAttributes) {
        HttpHeaders httpHeaders = requestAttributes.getRequestHeaders();
        List<Locale> locales = httpHeaders.getAcceptLanguageAsLocales();
        return first(locales);
    }

    private void resetContextHolders() {
        resetLocaleContext();
        resetRequestAttributes();
    }
}
