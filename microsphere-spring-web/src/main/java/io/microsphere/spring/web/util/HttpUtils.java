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

package io.microsphere.spring.web.util;

import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Set;

import static io.microsphere.collection.SetUtils.newFixedLinkedHashSet;
import static java.util.Collections.unmodifiableSet;
import static org.springframework.http.HttpMethod.values;

/**
 * The utilities class for HTTP
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HttpMethod
 * @since 1.0.0
 */
public abstract class HttpUtils {

    /**
     * All HTTP Methods: GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS and TRACE .
     */
    public static final Set<String> ALL_HTTP_METHODS;

    static {
        HttpMethod[] httpMethods = values();
        int size = httpMethods.length;
        Set<String> allHttpMethods = newFixedLinkedHashSet(size);
        for (int i = 0; i < size; i++) {
            allHttpMethods.add(httpMethods[i].name());
        }
        ALL_HTTP_METHODS = unmodifiableSet(allHttpMethods);
    }

    public static boolean supportsMethod(String method) {
        return method == null ? false : ALL_HTTP_METHODS.contains(method);
    }

    public static boolean supportsMethod(HttpMethod method) {
        return method == null ? false : ALL_HTTP_METHODS.contains(method.name());
    }

    public static boolean supportsMethod(RequestMethod method) {
        return method == null ? false : ALL_HTTP_METHODS.contains(method.name());
    }


    private HttpUtils() {
    }
}
