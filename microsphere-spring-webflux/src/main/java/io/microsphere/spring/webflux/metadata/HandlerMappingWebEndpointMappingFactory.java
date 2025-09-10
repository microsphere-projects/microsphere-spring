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

package io.microsphere.spring.webflux.metadata;


import io.microsphere.annotation.Nonnull;
import io.microsphere.spring.web.metadata.AbstractWebEndpointMappingFactory;
import io.microsphere.spring.web.metadata.HandlerMetadata;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingFactory;
import org.springframework.web.reactive.HandlerMapping;

import java.util.Collection;

import static io.microsphere.spring.web.metadata.WebEndpointMapping.webflux;
import static io.microsphere.util.Assert.assertNotNull;

/**
 * The abstract class {@link WebEndpointMappingFactory} for Spring WebFlux {@link HandlerMapping}
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

    /**
     * Constructor with {@link HandlerMapping}
     *
     * @param handlerMapping non-null {@link HandlerMapping} instance
     * @throws IllegalArgumentException If <code>handlerMapping</code> argument is null
     */
    public HandlerMappingWebEndpointMappingFactory(@Nonnull HandlerMapping handlerMapping) throws IllegalArgumentException {
        assertNotNull(handlerMapping, () -> "The 'handlerMapping' must not be null");
        this.handlerMapping = handlerMapping;
    }


    @Override
    protected final WebEndpointMapping<?> doCreate(HandlerMetadata<H, M> handlerMetadata) throws Throwable {
        HandlerMapping handlerMapping = this.handlerMapping;
        H handler = getHandler(handlerMetadata);
        M metadata = getMetadata(handlerMetadata);
        Collection<String> methods = getMethods(handler, metadata);
        Collection<String> patterns = getPatterns(handler, metadata);
        WebEndpointMapping.Builder builder = webflux()
                .endpoint(handler)
                .patterns(patterns)
                .methods(methods)
                .source(handlerMapping);
        contribute(handler, metadata, handlerMapping, builder);
        return builder.build();
    }

    protected H getHandler(HandlerMetadata<H, M> handlerMetadata) {
        return handlerMetadata.getHandler();
    }

    protected M getMetadata(HandlerMetadata<H, M> handlerMetadata) {
        return handlerMetadata.getMetadata();
    }

    /**
     * Get the methods of the specified {@link H Handler} and {@link M Metadata}
     *
     * @param handler  {@link H Handler}
     * @param metadata {@link M Metadata}
     * @return non-null
     */
    @Nonnull
    protected abstract Collection<String> getMethods(H handler, M metadata);

    /**
     * Get the patterns of the specified {@link H Handler} and {@link M Metadata}
     *
     * @param handler  {@link H Handler}
     * @param metadata {@link M Metadata}
     * @return non-null
     */
    @Nonnull
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

