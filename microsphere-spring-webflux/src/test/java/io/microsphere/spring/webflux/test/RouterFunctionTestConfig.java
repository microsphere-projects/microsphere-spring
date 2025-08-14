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

package io.microsphere.spring.webflux.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * {@link RouterFunction} Test Config
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RouterFunction
 * @since 1.0.0
 */
@Import(PersonHandler.class)
public class RouterFunctionTestConfig {

    public static final String PERSON_PATH = "/person";

    public static final String PERSON_ID_PATH = "/{id}";

    public static final String GET_PERSON_PATH = PERSON_PATH + PERSON_ID_PATH;

    @Bean
    public RouterFunction<ServerResponse> personRouterFunction(PersonHandler handler) {
        return route()
                .GET(GET_PERSON_PATH, accept(APPLICATION_JSON), handler::getPerson)
                .GET(PERSON_PATH, accept(APPLICATION_JSON), handler::listPeople)
                .POST(PERSON_PATH, handler::createPerson)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> nestedPersonRouterFunction(PersonHandler handler) {
        return route().path(PERSON_PATH, builder -> builder
                        .PUT(PERSON_ID_PATH, handler::updatePerson)
                        .DELETE(PERSON_ID_PATH, handler::deletePerson))
                .build();
    }
}
