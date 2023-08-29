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
package io.microsphere.spring.web.event;

import io.microsphere.spring.context.lifecycle.AbstractSmartLifecycle;
import io.microsphere.spring.web.annotation.EnableWebExtension;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistry;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import java.util.Collection;

/**
 * The class publishes the Spring Web extension events:
 * <ul>
 *     <li>{@link HandlerMethodArgumentsResolvedEvent}({@link EnableWebExtension#interceptHandlerMethods() if enabled})</li>
 *     <li>{@link WebEndpointMappingsReadyEvent}({@link EnableWebExtension#registerWebEndpointMappings() if enabled})</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethodArgumentsResolvedEvent
 * @since 1.0.0
 */
public class WebEventPublisher extends AbstractSmartLifecycle implements HandlerMethodInterceptor {

    public static final int DEFAULT_PHASE = EARLIEST_PHASE + 100;

    private final ApplicationContext context;

    public WebEventPublisher(ApplicationContext context) {
        this.context = context;
        setPhase(DEFAULT_PHASE);
    }

    @Override
    public void beforeExecute(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Exception {
        context.publishEvent(new HandlerMethodArgumentsResolvedEvent(request, handlerMethod, args));
    }

    @Override
    public void afterExecute(HandlerMethod handlerMethod, Object[] args, Object returnValue, Throwable error, NativeWebRequest request) throws Exception {
        // DO NOTHING
    }

    @Override
    protected void doStart() {
        WebEndpointMappingRegistry webEndpointMappingRegistry = context.getBean(WebEndpointMappingRegistry.class);
        Collection<WebEndpointMapping> webEndpointMappings = webEndpointMappingRegistry.getWebEndpointMappings();
        context.publishEvent(new WebEndpointMappingsReadyEvent(context, webEndpointMappings));
    }

    @Override
    protected void doStop() {
        // DO NOTHING
    }
}
