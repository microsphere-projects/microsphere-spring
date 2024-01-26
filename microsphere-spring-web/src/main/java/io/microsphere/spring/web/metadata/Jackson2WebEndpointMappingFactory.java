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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ClassUtils;

/**
 * The {@link WebEndpointMappingFactory} class based on Jackson2 for JSON
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class Jackson2WebEndpointMappingFactory extends AbstractWebEndpointMappingFactory<String> {

    private static final String OBJECT_MAPPER_CLASS_NAME = "com.fasterxml.jackson.databind.ObjectMapper";

    private static final ClassLoader classLoader = Jackson2WebEndpointMappingFactory.class.getClassLoader();

    private static final boolean objectMapperPresent = ClassUtils.isPresent(OBJECT_MAPPER_CLASS_NAME, classLoader);

    @Override
    public boolean supports(String endpoint) {
        return objectMapperPresent;
    }

    @Override
    protected WebEndpointMapping<String> doCreate(String endpoint) throws Throwable {
        WebEndpointMapping mapping = null;
        ObjectMapper objectMapper = new ObjectMapper();
        mapping = objectMapper.readValue(endpoint, WebEndpointMapping.class);
        return mapping;
    }
}
