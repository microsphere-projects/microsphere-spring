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


import io.microsphere.spring.test.web.controller.TestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.servlet.mvc.method.RequestMappingInfo.paths;

/**
 * {@link RequestMappingMetadata} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestMappingMetadata
 * @since 1.0.0
 */
class RequestMappingMetadataTest {

    private RequestMappingInfo requestMappingInfo;

    private HandlerMethod handlerMethod;

    private RequestMappingMetadata requestMappingMetadata;

    @BeforeEach
    void setUp() {
        this.requestMappingInfo = paths("/test")
                .methods(GET)
                .build();
        this.handlerMethod = new HandlerMethod(new TestController(), findMethod(TestController.class, "helloWorld"));
        this.requestMappingMetadata = new RequestMappingMetadata(this.requestMappingInfo, this.handlerMethod);
    }

    @Test
    void testGetHandler() {
        assertSame(this.handlerMethod, requestMappingMetadata.getHandler());
    }

    @Test
    void testGetMetadata() {
        assertSame(this.requestMappingInfo, requestMappingMetadata.getMetadata());
    }

    @Test
    void testGetHandlerMethod() {
        assertSame(requestMappingMetadata.getHandler(), requestMappingMetadata.getHandlerMethod());
    }

    @Test
    void testGetRequestMappingInfo() {
        assertSame(requestMappingMetadata.getMetadata(), requestMappingMetadata.getRequestMappingInfo());
    }
}