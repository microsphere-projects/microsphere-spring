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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ClassUtils;

/**
 * The {@link WebMappingDescriptorFactory} class based on Jackson2 for JSON
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class Jackson2WebMappingDescriptorFactory implements WebMappingDescriptorFactory<String> {

    private static final String OBJECT_MAPPER_CLASS_NAME = "com.fasterxml.jackson.databind.ObjectMapper";

    private static final ClassLoader classLoader = Jackson2WebMappingDescriptorFactory.class.getClassLoader();

    private static final boolean objectMapperPresent = ClassUtils.isPresent(OBJECT_MAPPER_CLASS_NAME, classLoader);

    @Override
    public boolean supports(String source) {
        return objectMapperPresent;
    }

    @Override
    public WebMappingDescriptor create(String source) {
        WebMappingDescriptor descriptor = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            descriptor = objectMapper.readValue(source, WebMappingDescriptor.class);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return descriptor;
    }
}
