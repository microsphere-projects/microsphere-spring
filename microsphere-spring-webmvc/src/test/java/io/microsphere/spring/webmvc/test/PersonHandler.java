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

package io.microsphere.spring.webmvc.test;

import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Person Handler
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServerRequest
 * @see ServerResponse
 * @since 1.0.0
 */
public class PersonHandler {

    public ServerResponse listPeople(ServerRequest request) {
        return null;
    }

    public ServerResponse createPerson(ServerRequest request) {
        return null;
    }

    public ServerResponse getPerson(ServerRequest request) {
        return null;
    }

    public ServerResponse updatePerson(ServerRequest request) {
        return null;
    }

    public ServerResponse deletePerson(ServerRequest request) {
        return null;
    }
}
