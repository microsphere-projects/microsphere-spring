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

import io.microsphere.spring.web.metadata.WebEndpointMappingFactory;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Collection;
import java.util.Collections;

/**
 * {@link WebEndpointMappingFactory} based on Spring WebMVC Handlers
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class HandlerMetadataWebEndpointMappingFactory extends HandlerMappingWebEndpointMappingFactory<Object, String> {

    public HandlerMetadataWebEndpointMappingFactory(HandlerMapping handlerMapping) {
        super(handlerMapping);
    }

    @Override
    protected Collection<String> getPatterns(Object handler, String metadata) {
        return Collections.singleton(metadata);
    }
}
