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

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link RequestMappingInfoWebEndpointMappingFactory} Test
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

    private RequestMappingInfoWebEndpointMappingFactory factory;

    @Autowired
    private RequestMappingInfoHandlerMapping handlerMapping;

    @BeforeEach
    public void init() {
        factory = new RequestMappingInfoWebEndpointMappingFactory();
        this.handlerMethods = handlerMapping.getHandlerMethods();
    }

    @Test
    public void test() {
        assertNotNull(handlerMethods);
        handlerMethods.keySet().forEach(requestMappingInfo -> {
            WebEndpointMapping webEndpointMapping = factory.create(requestMappingInfo);
            assertNotNull(webEndpointMapping.toJSON());
        });

    }

}
