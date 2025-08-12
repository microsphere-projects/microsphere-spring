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
package io.microsphere.spring.web.metadata;

import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import io.microsphere.spring.context.lifecycle.AbstractSmartLifecycle;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.event.WebEventPublisher;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collection;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * The abstract class registers all instances of {@link WebEndpointMapping} that are
 * collected from Web components into {@link WebEndpointMappingRegistry}
 * before {@link WebEventPublisher} publishing the {@link WebEndpointMappingsReadyEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMappingRegistry
 * @see WebEventPublisher
 * @see AbstractSmartLifecycle
 * @since 1.0.0
 */
public abstract class AbstractWebEndpointMappingRegistrar extends AbstractSmartLifecycle {

    protected final Logger logger = getLogger(getClass());

    protected final WebApplicationContext context;

    public AbstractWebEndpointMappingRegistrar(WebApplicationContext context) {
        this.context = context;
        // Mark sure earlier than WebEventPublisher
        setPhase(WebEventPublisher.DEFAULT_PHASE - 10);
    }

    @Override
    protected final void doStart() {
        registerWebEndpointMappings();
    }

    private void registerWebEndpointMappings() {
        WebEndpointMappingRegistry registry = getRegistry();
        Collection<WebEndpointMapping> webEndpointMappings = collectWebEndpointMappings();
        int count = registry.register(webEndpointMappings);
        logger.info("{} WebEndpointMappings were registered from the Spring context[id :'{}']", count, context.getId());
    }

    private WebEndpointMappingRegistry getRegistry() {
        return context.getBean(WebEndpointMappingRegistry.class);
    }

    /**
     * Collects all instances of {@link WebEndpointMapping} from the Web components.
     *
     * @return non-null
     */
    @Nonnull
    protected abstract Collection<WebEndpointMapping> collectWebEndpointMappings();

    @Override
    protected void doStop() {
    }
}
