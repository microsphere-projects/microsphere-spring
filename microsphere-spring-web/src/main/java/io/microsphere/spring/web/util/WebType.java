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

import jakarta.servlet.ServletRequest;
import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.util.Assert.assertNotNull;

/**
 * Web Type
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NativeWebRequest
 * @since 1.0.0
 */
public enum WebType {

    /**
     * The servlet-based web
     */
    SERVLET {
    },

    /**
     * The reactive web
     */
    REACTIVE {
    };

    /**
     * Resolve the {@link WebType} from the given {@link NativeWebRequest}.
     *
     * @param request the {@link NativeWebRequest} to resolve
     * @return the resolved {@link WebType}
     * @throws IllegalArgumentException if the request is null
     */
    public static WebType valueOf(NativeWebRequest request) {
        assertNotNull(request, () -> "The request must not be null");
        Object nativeRequest = request.getNativeRequest();
        if (nativeRequest instanceof ServletRequest) {
            return SERVLET;
        }
        return REACTIVE;
    }
}
