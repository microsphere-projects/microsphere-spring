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

import io.microsphere.spring.web.bind.annotation.RequestMappingMetadata;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.web.method.HandlerMethod;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Event raised when all {@link HandlerMethod HandlerMethods'} {@link RequestMappingMetadata} are ready.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestMappingMetadata
 * @since 1.0.0
 */
public class RequestMappingMetadataReadyEvent extends ApplicationContextEvent {

    private final Map<RequestMappingMetadata, HandlerMethod> metadata;

    public RequestMappingMetadataReadyEvent(ApplicationContext source, Map<RequestMappingMetadata, HandlerMethod> metadata) {
        super(source);
        this.metadata = unmodifiableMap(metadata);
    }

    public Map<RequestMappingMetadata, HandlerMethod> getMetadata() {
        return metadata;
    }
}
