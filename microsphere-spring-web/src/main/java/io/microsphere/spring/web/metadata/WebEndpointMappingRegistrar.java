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
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.List;

import static io.microsphere.collection.CollectionUtils.isNotEmpty;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;
import static io.microsphere.spring.core.io.support.SpringFactoriesLoaderUtils.loadFactories;
import static io.microsphere.util.Assert.assertNotNull;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;

/**
 * The class registers all instances of {@link WebEndpointMapping} that are
 * collected from Web components into {@link WebEndpointMappingRegistry}
 * before {@link WebEventPublisher} publishing the {@link WebEndpointMappingsReadyEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMappingRegistry
 * @see WebEndpointMappingResolver
 * @see WebEndpointMapping
 * @see WebEventPublisher
 * @see AbstractSmartLifecycle
 * @since 1.0.0
 */
public class WebEndpointMappingRegistrar extends AbstractSmartLifecycle {

    protected final Logger logger = getLogger(getClass());

    protected final ApplicationContext context;

    public WebEndpointMappingRegistrar(ApplicationContext context) {
        assertNotNull(context, () -> "ApplicationContext must not be null");
        this.context = context;
        // Mark sure earlier than WebEventPublisher
        setPhase(WebEventPublisher.DEFAULT_PHASE - 10);
    }

    @Override
    protected final void doStart() {
        registerWebEndpointMappings();
    }

    void registerWebEndpointMappings() {
        WebEndpointMappingRegistry registry = getRegistry();
        Collection<WebEndpointMapping> webEndpointMappings = resolveWebEndpointMappings();
        int count = registry.register(webEndpointMappings);
        this.logger.info("{} WebEndpointMappings were registered from the Spring context[id :'{}']", count, this.context.getId());
    }

    /**
     * Get the instance of {@link WebEndpointMappingRegistry}
     *
     * @return non-null
     */
    @Nonnull
    protected WebEndpointMappingRegistry getRegistry() {
        return this.context.getBean(WebEndpointMappingRegistry.class);
    }

    /**
     * Resolves all instances of {@link WebEndpointMapping} from the Web components.
     *
     * @return non-null
     */
    @Nonnull
    protected Collection<WebEndpointMapping> resolveWebEndpointMappings() {
        List<WebEndpointMapping> webEndpointMappings = newLinkedList();
        List<WebEndpointMappingResolver> resolvers = getWebEndpointMappingResolvers();
        ApplicationContext context = this.context;
        for (WebEndpointMappingResolver resolver : resolvers) {
            Collection<WebEndpointMapping> mappings = resolver.resolve(context);
            if (isNotEmpty(mappings)) {
                webEndpointMappings.addAll(resolver.resolve(context));
            }
        }
        return webEndpointMappings;
    }

    /**
     * Get the instances of {@link WebEndpointMappingResolver}
     *
     * @return non-null
     */
    @Nonnull
    protected List<WebEndpointMappingResolver> getWebEndpointMappingResolvers() {
        List<WebEndpointMappingResolver> resolvers = newLinkedList();
        ApplicationContext context = this.context;
        Class<WebEndpointMappingResolver> beanType = WebEndpointMappingResolver.class;
        resolvers.addAll(getSortedBeans(context, beanType));
        resolvers.addAll(loadFactories(context, beanType));
        sort(resolvers);
        return resolvers;
    }

    @Override
    protected void doStop() {
    }
}
