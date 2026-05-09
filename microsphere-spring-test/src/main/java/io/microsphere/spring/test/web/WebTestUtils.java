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

package io.microsphere.spring.test.web;

import io.microsphere.annotation.Nullable;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Map;

import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Locale.getDefault;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.get;
import static org.springframework.mock.web.server.MockServerWebExchange.from;
import static reactor.core.scheduler.Schedulers.isInNonBlockingThread;

/**
 * The utility class for testing in Spring Web
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class WebTestUtils {

    public static final String TEST_ROOT_PATH = "/test";

    public static final String ATTRIBUTE_NAME = "test-name";

    public static final String NOT_FOUND_ATTRIBUTE_NAME = "not-found-name";

    public static final String ATTRIBUTE_VALUE = "test-value";

    public static final String HEADER_NAME = "test-header-name";

    public static final String HEADER_VALUE = "test-header-value";

    public static final String HEADER_NAME_2 = "test-header-name-2";

    public static final String[] HEADER_VALUE_2 = ofArray("test-header-value-2", "test-header-value-3");

    public static final String PARAM_NAME = "test-param-name";

    public static final String PARAM_VALUE = "test-param-value";

    public static final String PARAM_NAME_2 = "test-param-name-2";

    public static final String[] PARAM_VALUE_2 = ofArray("test-param-value-2", "test-param-value-3");

    public static final String PERSON_PATH = "/person";

    public static final String PERSON_TEST_PATH = TEST_ROOT_PATH + PERSON_PATH;

    public static final String PERSON_ID_PATH = "/{id}";

    public static final String AUTH_NAME = "_auth";

    public static final String AUTH_VALUE = "123456789";

    public static final String GET_PERSON_PATH = PERSON_TEST_PATH + PERSON_ID_PATH;

    public static final InetSocketAddress REMOTE_ADDRESS = new InetSocketAddress("127.0.0.1", 12345);

    public static MockServerWebExchange mockServerWebExchange() {
        MockServerHttpRequest request = get(TEST_ROOT_PATH)
                .header(HEADER_NAME, HEADER_VALUE)
                .header(HEADER_NAME_2, HEADER_VALUE_2)
                .acceptLanguageAsLocales(getDefault())
                .remoteAddress(REMOTE_ADDRESS)
                .queryParam(PARAM_NAME, PARAM_VALUE)
                .queryParam(PARAM_NAME_2, PARAM_VALUE_2)
                .build();
        MockServerWebExchange serverWebExchange = from(request);
        Map<String, Object> attributes = serverWebExchange.getAttributes();
        attributes.put(ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

        WebSession webSession = getValue(serverWebExchange.getSession());
        webSession.getAttributes().put(ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

        return serverWebExchange;
    }

    /**
     * Get the emitted value from {@link Mono}
     *
     * @param mono {@link Mono}
     * @param <T>  the type of value
     * @return the emitted value
     */
    @Nullable
    public static <T> T getValue(Mono<T> mono) {
        if (isInNonBlockingThread()) {
            return execute(() -> mono.toFuture().get());
        } else {
            return mono.block();
        }
    }
}
