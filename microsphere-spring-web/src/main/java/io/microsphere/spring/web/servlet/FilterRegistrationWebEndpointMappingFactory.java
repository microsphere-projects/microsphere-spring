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
package io.microsphere.spring.web.servlet;

import io.microsphere.spring.web.metadata.AbstractWebEndpointMappingFactory;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingFactory;
import org.springframework.util.CollectionUtils;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import java.util.Collection;

import static io.microsphere.spring.web.metadata.WebEndpointMapping.of;

/**
 * {@link WebEndpointMappingFactory} from {@link FilterRegistration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServletContext#getFilterRegistrations()
 * @see FilterRegistration
 * @since 1.0.0
 */
public class FilterRegistrationWebEndpointMappingFactory extends AbstractWebEndpointMappingFactory<FilterRegistration> {

    public static final FilterRegistrationWebEndpointMappingFactory INSTANCE = new FilterRegistrationWebEndpointMappingFactory();

    @Override
    protected WebEndpointMapping<String> doCreate(FilterRegistration registration) {
        String filterName = registration.getName();
        Collection<String> mappings = registration.getUrlPatternMappings();
        if (CollectionUtils.isEmpty(mappings)) {
            // If filter mappings one or more servlets, the WebEndpointMappings will be generated from them.
            return null;
        }
        return of(filterName, mappings)
                .build();
    }
}
