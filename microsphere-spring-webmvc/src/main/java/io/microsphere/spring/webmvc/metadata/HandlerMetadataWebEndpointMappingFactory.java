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

import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.WEB_MVC;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.of;

/**
 * {@link WebEndpointMappingFactory} based on Spring WebMVC Handlers
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class HandlerMetadataWebEndpointMappingFactory extends AbstractWebEndpointMappingFactory<HandlerMetadata<Object, String>> {

    public static final HandlerMetadataWebEndpointMappingFactory INSTANCE = new HandlerMetadataWebEndpointMappingFactory();

    @Override
    protected WebEndpointMapping<Object> doCreate(HandlerMetadata<Object, String> source) {
        Object handler = source.getHandler();
        String url = source.getMetadata();
        return of(WEB_MVC, handler, url)
                .build();
    }
}
