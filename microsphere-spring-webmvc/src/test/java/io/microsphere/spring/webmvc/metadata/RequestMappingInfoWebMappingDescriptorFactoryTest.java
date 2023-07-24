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

import io.microsphere.spring.web.metadata.SmartWebMappingDescriptorFactory;
import io.microsphere.spring.web.metadata.WebMappingDescriptor;
import io.microsphere.spring.webmvc.method.PublishingHandlerMethodsEventListener;
import io.microsphere.spring.webmvc.method.RequestMappingInfoHandlerMethodsReadyEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link RequestMappingInfoWebMappingDescriptorFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        RequestMappingInfoWebMappingDescriptorFactoryTest.class
},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableAutoConfiguration
public class RequestMappingInfoWebMappingDescriptorFactoryTest {

    private Map<RequestMappingInfo, HandlerMethod> handlerMethods;

    private RequestMappingInfoWebMappingDescriptorFactory factory;

    @Autowired
    private RequestMappingInfoHandlerMapping handlerMapping;

    @BeforeEach
    public void init() {
        factory = new RequestMappingInfoWebMappingDescriptorFactory();
        this.handlerMethods = handlerMapping.getHandlerMethods();
    }

    @Test
    public void test() {
        assertNotNull(handlerMethods);
        handlerMethods.keySet().forEach(requestMappingInfo ->{
            WebMappingDescriptor webMappingDescriptor = factory.create(requestMappingInfo);
            System.out.println(webMappingDescriptor.toJSON());
        });

    }

}
