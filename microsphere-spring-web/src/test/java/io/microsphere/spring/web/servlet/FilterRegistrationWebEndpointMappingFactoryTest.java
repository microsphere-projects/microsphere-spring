///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package io.microsphere.spring.web.servlet;
//
//import io.microsphere.spring.web.metadata.WebEndpointMapping;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import javax.servlet.DispatcherType;
//import javax.servlet.FilterRegistration;
//import javax.servlet.ServletException;
//import java.util.EnumSet;
//import java.util.Optional;
//
//import static io.microsphere.util.ArrayUtils.of;
//import static io.microsphere.util.ArrayUtils.size;
//import static org.junit.jupiter.api.Assertions.assertArrayEquals;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
///**
// * {@link FilterRegistrationWebEndpointMappingFactory} Test
// *
// * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
// * @since 1.0.0
// */
//public class FilterRegistrationWebEndpointMappingFactoryTest {
//
//    private FilterRegistrationWebEndpointMappingFactory factory;
//
//    private String filterName;
//
//    private String url;
//
//    private FilterRegistration.Dynamic registration;
//
//    @BeforeEach
//    public void init() throws ServletException {
//        filterName = "test-filter";
//        url = "/test-filter";
//        this.factory = new FilterRegistrationWebEndpointMappingFactory(size())
//
//        this.registration = new TestFilterRegistration(filterName, "TestFilter");
//        this.registration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, this.url);
//    }
//
//    @Test
//    public void testCreate() {
//        Optional<WebEndpointMapping<?>> webEndpointMapping = factory.create(registration);
//        webEndpointMapping.ifPresent(mapping -> {
//            assertEquals(this.filterName, mapping.getEndpoint());
//            assertArrayEquals(of(this.url), mapping.getPatterns());
//        });
//    }
//}
