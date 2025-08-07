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

package io.microsphere.spring.webmvc.config;


import io.microsphere.spring.test.web.controller.TestRestController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ContentNegotiationManagerFactoryBean;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.accept.ParameterContentNegotiationStrategy;
import org.springframework.web.accept.PathExtensionContentNegotiationStrategy;
import org.springframework.web.accept.ServletPathExtensionContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.spring.webmvc.config.ConfigurableContentNegotiationManagerWebMvcConfigurerTest.MEDIA_TYPES_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * {@link ConfigurableContentNegotiationManagerWebMvcConfigurer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurableContentNegotiationManagerWebMvcConfigurer
 * @see ContentNegotiationManagerFactoryBean
 * @see ContentNegotiationManager
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        TestRestController.class,
        ConfigurableContentNegotiationManagerWebMvcConfigurer.class,
        ConfigurableContentNegotiationManagerWebMvcConfigurerTest.class
})
@TestPropertySource(
        properties = {
                "microsphere.spring.webmvc.content-negotiation.favorParameter=true",
                "microsphere.spring.webmvc.content-negotiation.parameterName=test-format",
                "microsphere.spring.webmvc.content-negotiation.favorPathExtension=true",
                "microsphere.spring.webmvc.content-negotiation.mediaTypes=" + MEDIA_TYPES_JSON,
                "microsphere.spring.webmvc.content-negotiation.ignoreUnknownPathExtensions=true",
                "microsphere.spring.webmvc.content-negotiation.useRegisteredExtensionsOnly=false",
                "microsphere.spring.webmvc.content-negotiation.ignoreAcceptHeader=false",
                "microsphere.spring.webmvc.content-negotiation.defaultContentType=application/json",
                "microsphere.spring.webmvc.content-negotiation.defaultContentTypes=" + MEDIA_TYPES_JSON,
                "microsphere.spring.webmvc.content-negotiation.defaultContentTypeStrategy=org.springframework.web.accept.HeaderContentNegotiationStrategy",

        }
)
@EnableWebMvc
public class ConfigurableContentNegotiationManagerWebMvcConfigurerTest {

    static final String MEDIA_TYPES_JSON = "{" +
            "  \"json\": \"application/json\"," +
            "  \"txt\": \"text/plain\"," +
            "  \"pdf\": \"application/pdf\"" +
            "}";

    @Autowired
    private ContentNegotiationManager contentNegotiationManager;

    @Test
    void testConfigureContentNegotiation() throws HttpMediaTypeNotAcceptableException {
        assertNotNull(this.contentNegotiationManager);


        List<ContentNegotiationStrategy> strategies = this.contentNegotiationManager.getStrategies();
        assertEquals(4, strategies.size());

        PathExtensionContentNegotiationStrategy pathExtensionContentNegotiationStrategy = this.contentNegotiationManager.getStrategy(ServletPathExtensionContentNegotiationStrategy.class);
        assertNotNull(pathExtensionContentNegotiationStrategy);

        ParameterContentNegotiationStrategy parameterContentNegotiationStrategy = this.contentNegotiationManager.getStrategy(ParameterContentNegotiationStrategy.class);
        assertNotNull(parameterContentNegotiationStrategy);
        assertEquals("test-format", parameterContentNegotiationStrategy.getParameterName());

        HeaderContentNegotiationStrategy headerContentNegotiationStrategy = this.contentNegotiationManager.getStrategy(HeaderContentNegotiationStrategy.class);
        assertNotNull(headerContentNegotiationStrategy);

        NativeWebRequest webRequest = createWebRequest(request -> request.addHeader(ACCEPT, APPLICATION_JSON_VALUE));
        List<MediaType> mediaTypes = headerContentNegotiationStrategy.resolveMediaTypes(webRequest);
        assertEquals(1, mediaTypes.size());
        assertTrue(mediaTypes.contains(APPLICATION_JSON));
    }
}