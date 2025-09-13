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
import io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension;
import io.microsphere.spring.webmvc.test.AbstractWebMvcTest;
import io.microsphere.spring.webmvc.test.SimpleUrlHandlerMappingTestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * {@link HandlerMappingWebEndpointMappingResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMappingWebEndpointMappingResolver
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        SimpleUrlHandlerMappingTestConfig.class,
        HandlerMappingWebEndpointMappingResolver.class,
        HandlerMappingWebEndpointMappingResolverTest.class,

})
@Import(TestController.class)
@EnableWebMvcExtension
public class HandlerMappingWebEndpointMappingResolverTest extends AbstractWebMvcTest {

    @Test
    public void testResolve() {
    }

    @Test
    public void testResolveFromAbstractUrlHandlerMapping() {
    }

    @Test
    public void testResolveFromRequestMappingInfoHandlerMapping() {
    }
}