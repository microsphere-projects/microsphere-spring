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
import io.microsphere.spring.web.metadata.WebEndpointMapping.Builder;
import io.microsphere.spring.web.metadata.WebEndpointMapping.Kind;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.Objects;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.io.IOUtils.copyToString;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Builder.assertBuilders;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Builder.pair;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.CUSTOMIZED;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.FILTER;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.SERVLET;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.WEB_FLUX;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.WEB_MVC;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.UNKNOWN_SOURCE;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.customized;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.filter;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.of;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.servlet;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.webflux;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.webmvc;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

/**
 * {@link WebEndpointMapping} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMapping
 * @since 1.0.0
 */
class WebEndpointMappingTest {

    public static final String[] TEST_URL_PATTERNS = ofArray("/test");

    public static final String[] TEST_METHODS = ofArray("OPTIONS");

    @Test
    void testServlet() {
        WebEndpointMapping mapping = minBuilder(servlet().endpoint(this)).build();
        assertMinMapping(mapping, SERVLET);
    }

    @Test
    void testFilter() {
        WebEndpointMapping mapping = minBuilder(filter().endpoint(this)).build();
        assertMinMapping(mapping, FILTER);
    }

    @Test
    void testWebMVC() {
        WebEndpointMapping mapping = minBuilder(webmvc().endpoint(this)).build();
        assertMinMapping(mapping, WEB_MVC);
    }

    @Test
    void testWebFlux() {
        WebEndpointMapping mapping = minBuilder(webflux().endpoint(this)).build();
        assertMinMapping(mapping, WEB_FLUX);
    }

    @Test
    void testCustomized() {
        WebEndpointMapping mapping = minBuilder(customized().endpoint(this)).build();
        assertMinMapping(mapping, CUSTOMIZED);
    }

    @Test
    void testBuildWithoutPatterns() {
        assertThrows(IllegalArgumentException.class, servlet().endpoint(this)::build);
    }

    @Test
    void testBuildWithoutMethods() {
        assertThrows(IllegalArgumentException.class, servlet().endpoint(this).patterns(TEST_URL_PATTERNS)::build);
    }

    @Test
    void testId() {
        WebEndpointMapping mapping = minServletBuilder().build();
        assertEquals(this.hashCode(), mapping.getId());
    }

    @Test
    void testPattern() {
        WebEndpointMapping mapping = of(SERVLET)
                .endpoint(this)
                .method(GET)
                .pattern("/test/1")
                .pattern("/test/2")
                .pattern("/test/3")
                .build();
        assertArrayEquals(ofArray("/test/1", "/test/2", "/test/3"), mapping.getPatterns());
    }

    @Test
    void testPatterns() {
        String[] patterns = ofArray("/a", "/b", "/c");
        WebEndpointMapping mapping = minServletBuilder()
                .patterns(patterns)
                .build();
        assertArrayEquals(patterns, mapping.getPatterns());
    }

    @Test
    void testPatternsWithCollection() {
        String[] patterns = ofArray("/a", "/b", "/c");
        WebEndpointMapping mapping = minServletBuilder()
                .patterns(ofList(patterns), Objects::toString)
                .build();
        assertArrayEquals(patterns, mapping.getPatterns());
    }

    @Test
    void testMethodWithHttpMethod() {
        WebEndpointMapping mapping = minServletBuilder()
                .method(POST)
                .build();
        assertArrayEquals(ofArray("OPTIONS", "POST"), mapping.getMethods());
    }

    @Test
    void testMethod() {
        WebEndpointMapping mapping = minServletBuilder()
                .method("PUT")
                .build();
        assertArrayEquals(ofArray("OPTIONS", "PUT"), mapping.getMethods());
    }

    @Test
    void testMethodsWithHttpMethods() {
        WebEndpointMapping mapping = minServletBuilder()
                .methods(GET, POST, PUT)
                .build();
        assertArrayEquals(ofArray("GET", "POST", "PUT"), mapping.getMethods());
    }

    @Test
    void testMethodsWithRequestMethods() {
        WebEndpointMapping mapping = minServletBuilder()
                .methods(ofList(RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT), RequestMethod::name)
                .build();
        assertArrayEquals(ofArray("GET", "POST", "PUT"), mapping.getMethods());
    }

    @Test
    void testMethods() {
        String[] methods = ofArray("GET", "DELETE");
        WebEndpointMapping mapping = minServletBuilder()
                .methods(methods)
                .build();
        assertArrayEquals(methods, mapping.getMethods());
    }

    @Test
    void testMethodsWithoutHttpMethods() {
        assertThrows(IllegalArgumentException.class, () -> minServletBuilder().methods((HttpMethod[]) null));
        assertThrows(IllegalArgumentException.class, () -> minServletBuilder().methods(new HttpMethod[0]));
        assertThrows(IllegalArgumentException.class, () -> minServletBuilder().methods(new HttpMethod[]{null}));
    }

    @Test
    void testMethodsWithoutMethods() {
        assertThrows(IllegalArgumentException.class, () -> minServletBuilder().methods((String[]) null));
        assertThrows(IllegalArgumentException.class, () -> minServletBuilder().methods(new String[0]));
        assertThrows(IllegalArgumentException.class, () -> minServletBuilder().methods(new String[]{null}));
    }

    @Test
    void testParam() {
        WebEndpointMapping mapping = minServletBuilder()
                .param("name1", "value1")
                .param("name2", "value2")
                .param("name3", "value3")
                .build();
        assertArrayEquals(ofArray("name1=value1", "name2=value2", "name3=value3"), mapping.getParams());
    }

    @Test
    void testParams() {
        String[] params = ofArray("p1", "p2", "p3");
        WebEndpointMapping mapping = minServletBuilder()
                .params(params)
                .build();
        assertArrayEquals(params, mapping.getParams());
    }

    @Test
    void testParamsWithCollection() {
        String[] params = ofArray("p1", "p2", "p3");
        WebEndpointMapping mapping = minServletBuilder()
                .params(ofList(params), Object::toString)
                .build();
        assertArrayEquals(params, mapping.getParams());
    }

    @Test
    void testParamsWithoutParams() {
        WebEndpointMapping mapping = minServletBuilder()
                .params()
                .build();
        assertArrayEquals(EMPTY_STRING_ARRAY, mapping.getParams());
    }

    @Test
    void testHeader() {
        WebEndpointMapping mapping = minServletBuilder()
                .header("h1", "v1")
                .header("h2", "v2")
                .header("h3", "v3")
                .build();
        assertArrayEquals(ofArray("h1=v1", "h2=v2", "h3=v3"), mapping.getHeaders());
    }

    @Test
    void testHeaderWithContentType() {
        WebEndpointMapping mapping = minServletBuilder()
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .build();
        assertArrayEquals(EMPTY_STRING_ARRAY, mapping.getHeaders());
        assertArrayEquals(ofArray(APPLICATION_JSON_VALUE), mapping.getConsumes());
    }

    @Test
    void testHeaderWithAccept() {
        WebEndpointMapping mapping = minServletBuilder()
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .build();
        assertArrayEquals(EMPTY_STRING_ARRAY, mapping.getHeaders());
        assertArrayEquals(ofArray(APPLICATION_JSON_VALUE), mapping.getProduces());
    }

    @Test
    void testHeaders() {
        String[] headers = ofArray("h1=v1", "h2=v2", "h3=v3");
        WebEndpointMapping mapping = minServletBuilder()
                .headers(headers)
                .build();
        assertArrayEquals(headers, mapping.getHeaders());
    }

    @Test
    void testHeadersWithCollection() {
        String[] headers = ofArray("h1=v1", "h2=v2", "h3=v3");
        WebEndpointMapping mapping = minServletBuilder()
                .headers(ofList(headers), Object::toString)
                .build();
        assertArrayEquals(headers, mapping.getHeaders());
    }

    @Test
    void testHeadersWithoutHeaders() {
        WebEndpointMapping mapping = minServletBuilder()
                .headers()
                .build();
        assertArrayEquals(EMPTY_STRING_ARRAY, mapping.getHeaders());
    }

    @Test
    void testConsumeWithMediaType() {
        WebEndpointMapping mapping = minServletBuilder()
                .consume(TEXT_PLAIN)
                .consume(APPLICATION_JSON)
                .consume(IMAGE_PNG)
                .build();
        assertArrayEquals(ofArray(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE, IMAGE_PNG_VALUE), mapping.getConsumes());
    }

    @Test
    void testConsume() {
        WebEndpointMapping mapping = minServletBuilder()
                .consume(TEXT_PLAIN_VALUE)
                .consume(APPLICATION_JSON_VALUE)
                .consume(IMAGE_PNG_VALUE)
                .build();
        assertArrayEquals(ofArray(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE, IMAGE_PNG_VALUE), mapping.getConsumes());
    }

    @Test
    void testConsumesWithMediaTypes() {
        WebEndpointMapping mapping = minServletBuilder()
                .consumes(TEXT_PLAIN, APPLICATION_JSON, IMAGE_PNG)
                .build();
        assertArrayEquals(ofArray(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE, IMAGE_PNG_VALUE), mapping.getConsumes());
    }

    @Test
    void testConsumes() {
        String[] consumes = ofArray(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE, IMAGE_PNG_VALUE);
        WebEndpointMapping mapping = minServletBuilder()
                .consumes(consumes)
                .build();
        assertArrayEquals(consumes, mapping.getConsumes());
    }

    @Test
    void testConsumesWithCollection() {
        String[] consumes = ofArray(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE, IMAGE_PNG_VALUE);
        WebEndpointMapping mapping = minServletBuilder()
                .consumes(ofList(consumes), Object::toString)
                .build();
        assertArrayEquals(consumes, mapping.getConsumes());
    }

    @Test
    void testConsumersWithoutConsumers() {
        WebEndpointMapping mapping = minServletBuilder().consumes((MediaType[]) null).build();
        assertSame(EMPTY_STRING_ARRAY, mapping.getConsumes());

        mapping = minServletBuilder().consumes(new MediaType[0]).build();
        assertSame(EMPTY_STRING_ARRAY, mapping.getConsumes());
    }

    @Test
    void testProduceWithMediaType() {
        WebEndpointMapping mapping = minServletBuilder()
                .produce(TEXT_PLAIN)
                .produce(APPLICATION_JSON)
                .produce(IMAGE_PNG)
                .build();
        assertArrayEquals(ofArray(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE, IMAGE_PNG_VALUE), mapping.getProduces());
    }

    @Test
    void testProduce() {
        WebEndpointMapping mapping = minServletBuilder()
                .produce(TEXT_PLAIN_VALUE)
                .produce(APPLICATION_JSON_VALUE)
                .produce(IMAGE_PNG_VALUE)
                .build();
        assertArrayEquals(ofArray(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE, IMAGE_PNG_VALUE), mapping.getProduces());
    }

    @Test
    void testProducesWithMediaTypes() {
        WebEndpointMapping mapping = minServletBuilder()
                .produces(TEXT_PLAIN, APPLICATION_JSON, IMAGE_PNG)
                .build();
        assertArrayEquals(ofArray(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE, IMAGE_PNG_VALUE), mapping.getProduces());
    }

    @Test
    void testProduces() {
        String[] produces = ofArray(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE, IMAGE_PNG_VALUE);
        WebEndpointMapping mapping = minServletBuilder()
                .produces(produces)
                .build();
        assertArrayEquals(produces, mapping.getProduces());
    }

    @Test
    void testProducesWithEmptyArray() {
        String[] produces = EMPTY_STRING_ARRAY;
        WebEndpointMapping mapping = minServletBuilder()
                .produces(produces)
                .build();
        assertArrayEquals(produces, mapping.getProduces());
    }

    @Test
    void testProducesWithCollection() {
        String[] produces = ofArray(TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE, IMAGE_PNG_VALUE);
        WebEndpointMapping mapping = minServletBuilder()
                .produces(ofList(produces), Object::toString)
                .build();
        assertArrayEquals(produces, mapping.getProduces());
    }

    @Test
    void testProducesOnDefault() {
        WebEndpointMapping mapping = minServletBuilder().build();
        assertSame(EMPTY_STRING_ARRAY, mapping.getProduces());
    }

    @Test
    void testIsNegated() {
        WebEndpointMapping mapping = minServletBuilder().build();
        assertFalse(mapping.isNegated());

        mapping = minServletBuilder().negate().build();
        assertTrue(mapping.isNegated());
    }

    @Test
    void testSource() {
        WebEndpointMapping mapping = minServletBuilder()
                .source(this)
                .build();
        assertEquals(this, mapping.getSource());
    }

    @Test
    void testNestPatterns() {
        WebEndpointMapping mapping = of(SERVLET)
                .endpoint(this)
                .pattern("/api")
                .method(GET)
                .nestPatterns(minServletBuilder()).build();
        assertArrayEquals(ofArray("/test/api"), mapping.getPatterns());
    }

    @Test
    void testNestMethods() {
        WebEndpointMapping mapping = minServletBuilder()
                .nestMethods(minServletBuilder().method(GET)).build();
        assertArrayEquals(ofArray("OPTIONS", "GET"), mapping.getMethods());
    }

    @Test
    void testNestParams() {
        WebEndpointMapping mapping = minServletBuilder()
                .nestParams(minServletBuilder().param("name1", "value1")).build();
        assertArrayEquals(ofArray("name1=value1"), mapping.getParams());
    }

    @Test
    void testNestHeaders() {
        WebEndpointMapping mapping = minServletBuilder()
                .header("name1", "value1")
                .nestHeaders(minServletBuilder().header("name1", "value1")).build();
        assertArrayEquals(ofArray("name1=value1"), mapping.getHeaders());
    }

    @Test
    void testNestConsumes() {
        WebEndpointMapping mapping = minServletBuilder()
                .consume(APPLICATION_JSON)
                .nestConsumes(minServletBuilder().consume(APPLICATION_XML)).build();
        assertArrayEquals(ofArray(APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE), mapping.getConsumes());
    }

    @Test
    void testNestProduces() {
        WebEndpointMapping mapping = minServletBuilder()
                .produce(APPLICATION_JSON)
                .nestProduces(minServletBuilder().produce(APPLICATION_XML)).build();
        assertArrayEquals(ofArray(APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE), mapping.getProduces());
    }

    @Test
    void testAttribute() {
        WebEndpointMapping mapping = minServletBuilder().build();
        assertNull(mapping.getAttribute("key"));

        mapping.setAttribute("key", null);
        assertNull(mapping.getAttribute("key"));

        mapping.setAttribute("key", "value");
        assertEquals("value", mapping.getAttribute("key"));

        mapping.setAttribute("key", "value-1");
        assertEquals("value-1", mapping.getAttribute("key"));
    }

    @Test
    void testBuilderToString() {
        Builder<?> builder = minServletBuilder();
        assertNotNull(builder.toString());
    }

    @Test
    void testEquals() {
        Builder<?> builder = minServletBuilder();
        WebEndpointMapping mapping = builder.build();
        // same instance
        assertEquals(mapping, mapping);
        // different instance
        assertNotEquals(mapping, null);
        assertNotEquals(mapping, this);

        // equals with different patterns
        assertNotEquals(mapping, minServletBuilder().pattern("/**").build());

        // equals with negated
        assertNotEquals(mapping, builder.negate().build());
        assertEquals(mapping, builder.negate().build());

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
        Builder<?> builder = minServletBuilder();
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
        Builder<?> builder = minServletBuilder();
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
        WebEndpointMapping mapping = of(CUSTOMIZED)
                .endpoint(1)
                .negate()
                .patterns("/a", "/b", "/c")
                .methods("GET", "POST")
                .params("a=1", "b=2")
                .headers("c=3", "d!=4")
                .consumes(APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE)
                .produces(TEXT_HTML_VALUE, TEXT_XML_VALUE)
                .build();

        assertEquals(fullJson, mapping.toJSON());
    }

    @Test
    void testToJSONSerialization() throws IOException {
        WebEndpointMapping mapping = of(CUSTOMIZED)
                .endpoint(1)
                .negate()
                .patterns("/a", "/b", "/c")
                .methods("GET", "POST")
                .params("a=1", "b=2")
                .headers("c=3", "d!=4")
                .consumes(APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE)
                .produces(TEXT_HTML_VALUE, TEXT_XML_VALUE)
                .build();

        String json = mapping.toJSON();
        ObjectMapper objectMapper = new ObjectMapper();
        WebEndpointMapping deserializedMapping = objectMapper.readValue(json, WebEndpointMapping.class);
        assertEquals(mapping, deserializedMapping);
    }

    @Test
    void testToJSONOnSteps() {
        Builder<?> builder = minServletBuilder();
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

    @Test
    void testPair() {
        assertEquals("a", pair("a", null));
        assertEquals("a=1", pair("a", 1));
    }

    @Test
    void testAssertBuilders() {
        assertBuilders(minServletBuilder(), minServletBuilder());
        assertBuilders(minServletBuilder(), minBuilder(SERVLET));

        assertThrows(IllegalArgumentException.class, () -> assertBuilders(null, null));
        assertThrows(IllegalArgumentException.class, () -> assertBuilders(minServletBuilder(), null));
        assertThrows(IllegalArgumentException.class, () -> assertBuilders(minServletBuilder(), minBuilder(FILTER)));
    }


    Builder<?> minServletBuilder() {
        return minBuilder(SERVLET);
    }

    Builder<?> minBuilder(Kind kind) {
        return minBuilder(of(kind).endpoint(this));
    }

    Builder<?> minBuilder(Builder<?> builder) {
        return builder.patterns(TEST_URL_PATTERNS)
                .methods(TEST_METHODS);
    }

    void assertMinMapping(WebEndpointMapping mapping, Kind kind) {
        assertSame(kind, mapping.getKind());
        assertSame(this, mapping.getEndpoint());
        assertSame(UNKNOWN_SOURCE, mapping.getSource());
        assertArrayEquals(TEST_URL_PATTERNS, mapping.getPatterns());
        assertArrayEquals(TEST_METHODS, mapping.getMethods());
        assertSame(EMPTY_STRING_ARRAY, mapping.getParams());
        assertSame(EMPTY_STRING_ARRAY, mapping.getHeaders());
        assertSame(EMPTY_STRING_ARRAY, mapping.getConsumes());
        assertSame(EMPTY_STRING_ARRAY, mapping.getProduces());
    }

}
