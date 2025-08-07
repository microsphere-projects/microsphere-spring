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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.CUSTOMIZED;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.SERVLET;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.WEB_FLUX;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.NON_ENDPOINT;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.NON_SOURCE;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.of;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.util.StreamUtils.copyToString;

/**
 * {@link WebEndpointMapping} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMapping
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebEndpointMappingTest.class)
public class WebEndpointMappingTest {

    @Test
    void testOfWithCollection() {
        WebEndpointMapping mapping = of(ofList("/*")).build();
        assertSame(CUSTOMIZED, mapping.getKind());
        assertSame(NON_ENDPOINT, mapping.getEndpoint());
        assertSame(NON_SOURCE, mapping.getSource());
        assertArrayEquals(ofArray("/*"), mapping.getPatterns());
    }

    @Test
    void testOfWithEndpointAndCollection() {
        WebEndpointMapping mapping = of(this, ofList("/*")).build();
        assertSame(CUSTOMIZED, mapping.getKind());
        assertSame(this, mapping.getEndpoint());
        assertSame(NON_SOURCE, mapping.getSource());
        assertArrayEquals(ofArray("/*"), mapping.getPatterns());
    }

    @Test
    void testOfWithKindAndEndpointAndCollection() {
        WebEndpointMapping mapping = of(SERVLET, this, ofList("/*")).build();
        assertSame(SERVLET, mapping.getKind());
        assertSame(this, mapping.getEndpoint());
        assertSame(NON_SOURCE, mapping.getSource());
        assertArrayEquals(ofArray("/*"), mapping.getPatterns());
    }

    @Test
    void testOfWithArray() {
        WebEndpointMapping mapping = of("/*").build();
        assertSame(CUSTOMIZED, mapping.getKind());
        assertSame(NON_ENDPOINT, mapping.getEndpoint());
        assertSame(NON_SOURCE, mapping.getSource());
        assertArrayEquals(ofArray("/*"), mapping.getPatterns());
    }

    @Test
    void testOfWithEndpointAndArray() {
        WebEndpointMapping mapping = of(this, "/*").build();
        assertSame(CUSTOMIZED, mapping.getKind());
        assertSame(this, mapping.getEndpoint());
        assertSame(NON_SOURCE, mapping.getSource());
        assertArrayEquals(ofArray("/*"), mapping.getPatterns());
    }

    @Test
    void testOfWithKindAndEndpointAndArray() {
        WebEndpointMapping mapping = of(WEB_FLUX, this, "/*").build();
        assertSame(WEB_FLUX, mapping.getKind());
        assertSame(this, mapping.getEndpoint());
        assertSame(NON_SOURCE, mapping.getSource());
        assertArrayEquals(ofArray("/*"), mapping.getPatterns());
    }

    @Test
    void testId() {
        WebEndpointMapping mapping = of(this, "/*").build();
        assertEquals(this.hashCode(), mapping.getId());
    }

    @Test
    void testSource() {
        WebEndpointMapping mapping = of("/*").source(this).build();
        assertEquals(this, mapping.getSource());
    }

    @Test
    void testPatterns() {
        String[] patterns = ofArray("/a", "/b", "/c");
        WebEndpointMapping mapping = of(patterns).build();
        assertArrayEquals(ofArray(patterns), mapping.getPatterns());
    }

    @Test
    void testMethods() {
        String[] methods = ofArray("GET", "POST");
        WebEndpointMapping mapping = of("/*")
                .methods(methods)
                .build();

        assertArrayEquals(methods, mapping.getMethods());

        mapping = of("/*")
                .methods(ofList(GET, POST), HttpMethod::name)
                .build();

        assertArrayEquals(methods, mapping.getMethods());
    }

    @Test
    void testMethodsOnDefault() {
        WebEndpointMapping mapping = of("/*").build();
        assertSame(EMPTY_STRING_ARRAY, mapping.getMethods());
    }

    @Test
    void testParams() {
        String[] params = ofArray("p1", "p2", "p3");
        WebEndpointMapping mapping = of("/*")
                .params(params)
                .build();

        assertArrayEquals(params, mapping.getParams());

        mapping = of("/*")
                .params(ofList(params), String::valueOf)
                .build();

        assertArrayEquals(params, mapping.getParams());
    }

    @Test
    void testParamsOnDefault() {
        WebEndpointMapping mapping = of("/*").build();
        assertSame(EMPTY_STRING_ARRAY, mapping.getParams());
    }

    @Test
    void testHeaders() {
        String[] headers = ofArray("p1", "p2", "p3");
        WebEndpointMapping mapping = of("/*")
                .headers(headers)
                .build();

        assertArrayEquals(headers, mapping.getHeaders());

        mapping = of("/*")
                .headers(ofList(headers), String::valueOf)
                .build();

        assertArrayEquals(headers, mapping.getHeaders());

        mapping = of("/*")
                .headers(emptyList(), String::valueOf)
                .build();

        assertArrayEquals(EMPTY_STRING_ARRAY, mapping.getHeaders());
    }

    @Test
    void testHeadersOnDefault() {
        WebEndpointMapping mapping = of("/*").build();
        assertSame(EMPTY_STRING_ARRAY, mapping.getHeaders());
    }

    @Test
    void testConsumes() {
        String[] consumes = ofArray("p1", "p2", "p3");
        WebEndpointMapping mapping = of("/*")
                .consumes(consumes)
                .build();

        assertArrayEquals(consumes, mapping.getConsumes());

        mapping = of("/*")
                .consumes(ofList(consumes), String::valueOf)
                .build();

        assertArrayEquals(consumes, mapping.getConsumes());
    }

    @Test
    void testConsumersOnDefault() {
        WebEndpointMapping mapping = of("/*").build();
        assertSame(EMPTY_STRING_ARRAY, mapping.getConsumes());
    }

    @Test
    void testProduces() {
        String[] produces = ofArray("p1", "p2", "p3");
        WebEndpointMapping mapping = of("/*")
                .produces(produces)
                .build();

        assertArrayEquals(produces, mapping.getProduces());

        mapping = of("/*")
                .produces(ofList(produces), String::valueOf)
                .build();

        assertArrayEquals(produces, mapping.getProduces());
    }

    @Test
    void testProducesOnDefault() {
        WebEndpointMapping mapping = of("/*").build();
        assertSame(EMPTY_STRING_ARRAY, mapping.getProduces());
    }

    @Test
    void testAttribute() {
        WebEndpointMapping mapping = of("/*").build();
        assertNull(mapping.getAttribute("key"));

        mapping.setAttribute("key", null);
        assertNull(mapping.getAttribute("key"));

        mapping.setAttribute("key", "value");
        assertEquals("value", mapping.getAttribute("key"));
    }

    @Test
    void testEquals() {
        WebEndpointMapping.Builder<?> builder = of("/*");
        WebEndpointMapping mapping = builder.build();
        // same instance
        assertEquals(mapping, mapping);
        // different instance
        assertNotEquals(mapping, null);
        assertNotEquals(mapping, this);

        // equals with different patterns
        assertNotEquals(mapping, of("/**").build());

        // equals with patterns
        assertEquals(mapping, builder.build());

        // equals with patterns and methods
        builder.methods("GET");
        assertEquals(builder.build(), builder.build());
        assertNotEquals(mapping, (mapping = builder.build()));

        // equals with patterns, methods and params
        builder.params("p1");
        assertEquals(builder.build(), builder.build());
        assertNotEquals(mapping, (mapping = builder.build()));

        // equals with patterns, methods, params and headers
        builder.headers("h1");
        assertEquals(builder.build(), builder.build());
        assertNotEquals(mapping, (mapping = builder.build()));

        // equals with patterns, methods, params, headers and consumes
        builder.consumes("c1");
        assertEquals(builder.build(), builder.build());
        assertNotEquals(mapping, (mapping = builder.build()));

        // equals with patterns, methods, params, headers, consumes and produces
        builder.produces("p1");
        assertEquals(builder.build(), builder.build());
        assertNotEquals(mapping, builder.build());
    }

    @Test
    void testHashCode() {
        WebEndpointMapping.Builder<?> builder = of("/*");
        WebEndpointMapping mapping = builder.build();
        // same instance
        assertEquals(mapping.hashCode(), mapping.hashCode());
        // different instance
        assertNotEquals(this.hashCode(), mapping.hashCode());

        // hashCode with patterns
        assertEquals(mapping.hashCode(), builder.build().hashCode());

        // hashCode with patterns and methods
        builder.methods("GET");
        assertEquals(builder.build().hashCode(), builder.build().hashCode());

        // hashCode with patterns, methods and params
        builder.params("p1");
        assertEquals(builder.build().hashCode(), builder.build().hashCode());

        // hashCode with patterns, methods, params and headers
        builder.headers("h1");
        assertEquals(builder.build().hashCode(), builder.build().hashCode());

        // hashCode with patterns, methods, params, headers and consumes
        builder.consumes("c1");
        assertEquals(builder.build().hashCode(), builder.build().hashCode());

        // hashCode with patterns, methods, params, headers, consumes and produces
        builder.produces("p1");
        assertEquals(builder.build().hashCode(), builder.build().hashCode());
    }

    @Test
    void testToString() {
        WebEndpointMapping.Builder<?> builder = of("/*");
        WebEndpointMapping mapping = builder.build();
        // same instance
        assertEquals(mapping.toString(), mapping.toString());
        // different instance
        assertNotEquals(this.toString(), mapping.toString());

        // hashCode with patterns
        assertEquals(mapping.toString(), builder.build().toString());

        // hashCode with patterns and methods
        builder.methods("GET");
        assertEquals(builder.build().toString(), builder.build().toString());

        // hashCode with patterns, methods and params
        builder.params("p1");
        assertEquals(builder.build().toString(), builder.build().toString());

        // hashCode with patterns, methods, params and headers
        builder.headers("h1");
        assertEquals(builder.build().toString(), builder.build().toString());

        // hashCode with patterns, methods, params, headers and consumes
        builder.consumes("c1");
        assertEquals(builder.build().toString(), builder.build().toString());

        // hashCode with patterns, methods, params, headers, consumes and produces
        builder.produces("p1");
        assertEquals(builder.build().toString(), builder.build().toString());
    }


    @Test
    void testToJSON() throws IOException {
        Resource fullJsonResource = new DefaultResourceLoader().getResource("classpath:META-INF/web-mapping-descriptor.json");
        String fullJson = copyToString(fullJsonResource.getInputStream(), UTF_8);
        WebEndpointMapping mapping = of(CUSTOMIZED, 1, "/a", "/b", "/c")
                .methods("GET", "POST")
                .params("a=1", "b=2")
                .headers("c=3", "d!=4")
                .consumes("application/json", "application/xml")
                .produces("text/html", "text/xml")
                .build();

        assertEquals(fullJson, mapping.toJSON());
    }

    @Test
    void testToJSONOnSteps() {
        WebEndpointMapping.Builder<?> builder = of("/*");
        WebEndpointMapping mapping = builder.build();
        // same instance
        assertEquals(mapping.toJSON(), mapping.toJSON());

        // toJSON with patterns
        assertEquals(mapping.toJSON(), builder.build().toJSON());

        // toJSON with patterns and methods
        builder.methods("GET");
        assertEquals(builder.build().toJSON(), builder.build().toJSON());

        // toJSON with patterns, methods and params
        builder.params("p1");
        assertEquals(builder.build().toJSON(), builder.build().toJSON());

        // toJSON with patterns, methods, params and headers
        builder.headers("h1");
        assertEquals(builder.build().toJSON(), builder.build().toJSON());

        // toJSON with patterns, methods, params, headers and consumes
        builder.consumes("c1");
        assertEquals(builder.build().toJSON(), builder.build().toJSON());

        // toJSON with patterns, methods, params, headers, consumes and produces
        builder.produces("p1");
        assertEquals(builder.build().toJSON(), builder.build().toJSON());
    }

}
