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
package io.microsphere.spring.webmvc.metadata;

import io.microsphere.spring.web.metadata.AbstractWebEndpointMappingFactory;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingFactory;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Collection;

import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.WEB_MVC;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.of;

/**
 * The abstract class {@link WebEndpointMappingFactory} for Spring WebMVC {@link HandlerMapping}
 *
 * @param <H> the type of handler
 * @param <M> the type of metadata
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMapping
 * @see AbstractWebEndpointMappingFactory
 * @since 1.0.0
 */
public abstract class HandlerMappingWebEndpointMappingFactory<H, M> extends AbstractWebEndpointMappingFactory<HandlerMetadata<H, M>> {

    private final HandlerMapping handlerMapping;

    public HandlerMappingWebEndpointMappingFactory(HandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    protected final WebEndpointMapping<?> doCreate(HandlerMetadata<H, M> handlerMetadata) throws Throwable {
        HandlerMapping handlerMapping = this.handlerMapping;
        H handler = handlerMetadata.getHandler();
        M metadata = handlerMetadata.getMetadata();
        Collection<String> patterns = getPatterns(handler, metadata);
        WebEndpointMapping.Builder builder = of(WEB_MVC, handler, patterns);
        builder.source(handlerMapping);
        contribute(handler, metadata, handlerMapping, builder);
        return builder.build();
    }

    /**
     * Get the patterns of {@link H Handler} and {@link M Metadata}
     *
     * @param handler  {@link H Handler}
     * @param metadata {@link M Metadata}
     * @return non-null
     */
    protected abstract Collection<String> getPatterns(H handler, M metadata);

    /**
     * Contribute the {@link WebEndpointMapping.Builder} to create an instance of {@link WebEndpointMapping}
     *
     * @param handler        {@link H Handler}
     * @param metadata       {@link M Metadata}
     * @param handlerMapping {@link HandlerMapping}
     * @param builder        {@link WebEndpointMapping.Builder}
     */
    protected void contribute(H handler, M metadata, HandlerMapping handlerMapping,
                              WebEndpointMapping.Builder<H> builder) {
        // The sub-class implements the current method
    }
}
