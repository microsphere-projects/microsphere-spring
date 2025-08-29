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

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.empty;

/**
 * Person Handler
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServerRequest
 * @see ServerResponse
 * @since 1.0.0
 */
public class PersonHandler {

    public Mono<ServerResponse> listPeople(ServerRequest request) {
        return empty();
    }

    public Mono<ServerResponse> createPerson(ServerRequest request) {
        return empty();
    }

    public Mono<ServerResponse> getPerson(ServerRequest request) {
        return empty();
    }

    public Mono<ServerResponse> updatePerson(ServerRequest request) {
        return empty();
    }

    public Mono<ServerResponse> deletePerson(ServerRequest request) {
        return empty();
    }
}
