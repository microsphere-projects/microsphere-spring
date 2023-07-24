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

import org.springframework.util.Assert;

import java.util.Objects;

/**
 * The metadata class for Spring WebMVC's Handler
 *
 * @param <H> the type of handler
 * @param <M> the type of metadata
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class HandlerMetadata<H, M> {

    private final H handler;

    private final M metadata;

    public HandlerMetadata(H handler, M metadata) {
        Assert.notNull(handler, "The 'handler' must not be null!");
        Assert.notNull(metadata, "The 'metadata' must not be null!");
        this.handler = handler;
        this.metadata = metadata;
    }

    public H getHandler() {
        return handler;
    }

    public M getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandlerMetadata<?, ?> that = (HandlerMetadata<?, ?>) o;
        return Objects.equals(handler, that.handler) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handler, metadata);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HandlerMetadata{");
        sb.append("handler=").append(handler);
        sb.append(", metadata=").append(metadata);
        sb.append('}');
        return sb.toString();
    }
}
