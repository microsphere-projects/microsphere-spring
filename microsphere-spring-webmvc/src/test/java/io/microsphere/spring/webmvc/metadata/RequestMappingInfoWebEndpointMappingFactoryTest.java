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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RequestMappingMetadataWebEndpointMappingFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        RequestMappingInfoWebEndpointMappingFactoryTest.class
},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class RequestMappingInfoWebEndpointMappingFactoryTest {

    private Map<RequestMappingInfo, HandlerMethod> handlerMethods;

    private RequestMappingMetadataWebEndpointMappingFactory factory;

    @Autowired
    private RequestMappingInfoHandlerMapping requestMappingInfoHandlerMapping;

    @BeforeEach
    public void init() {
        factory = new RequestMappingMetadataWebEndpointMappingFactory(requestMappingInfoHandlerMapping);
        this.handlerMethods = requestMappingInfoHandlerMapping.getHandlerMethods();
    }

    @Test
    public void test() {
        assertNotNull(handlerMethods);
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingMetadata metadata = new RequestMappingMetadata(entry.getKey(), entry.getValue());
            Optional<WebEndpointMapping<HandlerMetadata<HandlerMethod, RequestMappingInfo>>> webEndpointMapping = factory.create(metadata);
            assertTrue(webEndpointMapping.isPresent());
            webEndpointMapping.ifPresent(mapping -> {
                assertEquals(this.requestMappingInfoHandlerMapping, mapping.getSource());
                assertNotNull(webEndpointMapping.get().toJSON());
            });
        }
    }

}
