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

import io.microsphere.spring.web.metadata.WebEndpointMapping;
import org.springframework.util.Assert;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Objects;

/**
 * The metadata of {@link DispatcherServlet}'s handler and {@link WebEndpointMapping}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DispatcherServlet
 * @see WebEndpointMapping
 * @since 1.0.0
 */
public class HandlerMappingDescriptor {

    private final Object handler;

    private final WebEndpointMapping descriptor;

    public HandlerMappingDescriptor(Object handler, WebEndpointMapping descriptor) {
        Assert.notNull(handler,"The 'handler' argument must not be null");
        Assert.notNull(descriptor,"The 'descriptor' argument must not be null");
        this.handler = handler;
        this.descriptor = descriptor;
    }

    public Object getHandler() {
        return handler;
    }

    public WebEndpointMapping getDescriptor() {
        return descriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandlerMappingDescriptor that = (HandlerMappingDescriptor) o;
        return Objects.equals(handler, that.handler) && Objects.equals(descriptor, that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handler, descriptor);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HandlerMappingDescriptor{");
        sb.append("handler=").append(handler);
        sb.append(", descriptor=").append(descriptor);
        sb.append('}');
        return sb.toString();
    }
}
